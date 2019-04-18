/**
 * 在使用rxjava调用接口时,可以使用单独接口返回onNext()和单独接口返回onError()
 * <p>
 * 如果错误可以统一处理展示的话,可以自定义抽象类,在类中统一处理错误,同时把成功返回的信息交给view处理
 * <p>
 * 如果要添加progressdialog的话,也可以自定义Observer抽象类实现Observer,统一处理
 */
package http;
