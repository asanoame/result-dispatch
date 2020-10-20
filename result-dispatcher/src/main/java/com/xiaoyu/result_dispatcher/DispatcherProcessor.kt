package com.xiaoyu.result_dispatcher

import com.squareup.javapoet.*
import java.io.IOException
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

@SupportedAnnotationTypes("com.xiaoyu.result_dispatcher.ResultDispatch")
@SupportedOptions("requestCode", "resultCode")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class DispatcherProcessor : AbstractProcessor() {
    private lateinit var mMessage: Messager
    private lateinit var mElementsUtil: Elements
    private lateinit var mMessageBuilder: StringBuilder
    private lateinit var mDispatcherElements: MutableMap<TypeElement, MutableSet<Element>>

    override fun init(enviorment: ProcessingEnvironment) {
        super.init(enviorment)
        mMessage = enviorment.messager
        mElementsUtil = enviorment.elementUtils
        mMessageBuilder = StringBuilder()
        mDispatcherElements = mutableMapOf()
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnvironment: RoundEnvironment
    ): Boolean {
        initAnnotations(roundEnvironment.getElementsAnnotatedWith(ResultDispatch::class.java))
        generateJavaClass()
        mMessage.printMessage(Diagnostic.Kind.NOTE, mMessageBuilder)
        return true
    }

    private fun initAnnotations(elements: Set<Element>) {
        printf("处理注解信息")
        for (element in elements) {
            val enclosingElement = element.enclosingElement
            if (enclosingElement is TypeElement) {
                var set = mDispatcherElements[enclosingElement]
                if (set == null) {
                    set = mutableSetOf()
                    mDispatcherElements[enclosingElement] = set
                }
                if (!set.contains(element)) {
                    set.add(element)
                }
            } else {
                continue
            }
        }
    }

    private fun generateJavaClass() {
        printf("生成java类")
        //先生成每个Activity或者Fragment分发是需要的类
        for (enclosedElement in mDispatcherElements.keys) {
            printf("生成${enclosedElement.simpleName}的分发类")
            val mutableSet = mDispatcherElements[enclosedElement] ?: continue
            //默认的三个参数 requestCode resultCode Intent
            val parameters = mutableListOf(
                ParameterSpec.builder(Int::class.java, "requestCode").build(),
                ParameterSpec.builder(Int::class.java, "resultCode").build(),
                ParameterSpec.builder(Class.forName("android.content.Intent"), "data")
                    .build()
            )
            //生成分发方法
            val name = enclosedElement.simpleName.toString().decapitalize(Locale.ROOT)
            val methodBuilder = MethodSpec.methodBuilder("dispatch")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(
                    ParameterSpec.builder(
                        ClassName.get(enclosedElement.asType()), name
                    ).build()
                )
                .addParameters(parameters)
                .returns(TypeName.VOID)
            val contentBuilder = StringBuilder()
            val iterator = mutableSet.iterator()
            while (iterator.hasNext()) {
                val element = iterator.next()
                val dispatcher = element.getAnnotation(ResultDispatch::class.java)
                val haveData = element.toString().contains("android.content.Intent")
                printf("requestCode=${dispatcher.requestCode},resultCode=${dispatcher.resultCode},haveData=$haveData,${element}")
                contentBuilder.append(
                    "if (requestCode == ${dispatcher.requestCode} && resultCode == ${dispatcher.resultCode}) {\n" +
                            "$name.${element.simpleName}(${if (haveData) "data" else ""});\n" +
                            "} ${if (iterator.hasNext()) "else" else ""} "
                )
            }
            methodBuilder.addStatement(CodeBlock.of(contentBuilder.toString()))
            try {
                val typeSpec =
                    TypeSpec.classBuilder("${enclosedElement.simpleName}Dispatcher")
                        .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                        .addMethod(methodBuilder.build())
                        .build()
                val file = JavaFile.builder(getPackageName(enclosedElement), typeSpec).build()
                file.writeTo(processingEnv.filer)
            } catch (e: IOException) {
            }
        }
        //生成统一的代理入口（方便在base里调用）
        val parameters = mutableListOf(
            ParameterSpec.builder(Any::class.java, "object").build(),
            ParameterSpec.builder(Int::class.java, "requestCode").build(),
            ParameterSpec.builder(Int::class.java, "resultCode").build(),
            ParameterSpec.builder(Class.forName("android.content.Intent"), "data")
                .build()
        )
        val fileBuilder = TypeSpec.classBuilder("Dispatcher")
            .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
        val methodBuilder = MethodSpec.methodBuilder("dispatch")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameters(parameters)
            .returns(TypeName.VOID)

        val contentBuilder = StringBuilder()
        val iterator = mDispatcherElements.keys.iterator()
        while (iterator.hasNext()) {
            val enclosedElement = iterator.next()
            contentBuilder.append(
                "if (object instanceof ${enclosedElement.qualifiedName}) {\n" +
                        "${enclosedElement.qualifiedName}Dispatcher.dispatch((${enclosedElement.qualifiedName})object,requestCode,resultCode,data);\n" +
                        "}${if (iterator.hasNext()) "else" else ""} "
            )
        }
        methodBuilder.addStatement(CodeBlock.of(contentBuilder.toString()))
        fileBuilder.addMethod(methodBuilder.build())
        val file = JavaFile.builder("com.xiaoyu.resultdispatch", fileBuilder.build()).build()
        try {
            file.writeTo(processingEnv.filer)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getPackageName(typeElement: TypeElement): String {
        return mElementsUtil.getPackageOf(typeElement).qualifiedName.toString()
    }

    private fun printf(message: Any) {
        mMessageBuilder.append("\n$message")
    }
}