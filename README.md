> 在github上发不了新版本，但是引用下载就同步失败了。知道是为什么，有小伙伴知道问题出在哪里的话方便告诉一下

将源码下载下来并导入到工程里，在需要的module里添加代码
```
apply plugin: 'kotlin-kapt'
dependencies {
    implementation project(path: ':annotation')
    kapt project(path: ':processor')
}
```
然后在Activity或者Fragment 里 定义方法
```
@ResultDispatch(10, 20)
fun testDispatcher() {
    Log.d("Main", "回来了，没有Intent")
}

@ResultDispatch(10, 30)
fun testDispatcher(intent: Intent?) {
    Log.d("Main", "wow,这是传递过来的信息${intent?.getStringExtra("text")}")
}
```
重写一下onActivityResult
```
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        MainActivityDispatcher.dispatch(this, requestCode, resultCode, data)
}
```
其中 `MainActivityDispatcher`是框架自动生成的,看一下里面的内容
```
public final class MainActivityDispatcher {
  public static void dispatch(MainActivity mainActivity, int requestCode, int resultCode,
      Intent data) {
    if (requestCode == 10 && resultCode == 20) {
        mainActivity.testDispatcher();
    } else if (requestCode == 10 && resultCode == 30) {
        mainActivity.testDispatcher(data);
    }  
  }
}
```
只有一个dispatch的方法，里面的code判断由编译器帮我们自动生成，并且判断了我们需不需要intent，是不是方便了许多？
> 其实原来打算 onActivityResult这个方法也是用编译器生成，但是功力不到家，只能换方法实现，哪位大佬知道方法请告知小弟一下（土下座）。。。

啊，还有，为什么github发布的版本引入不了啊。呜呜呜呜
