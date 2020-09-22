package com.xiaoyu.annotation

/**
 *[requestCode] 请求值
 * [resultCode] 返回值
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class ResultDispatch(val requestCode: Int, val resultCode: Int)