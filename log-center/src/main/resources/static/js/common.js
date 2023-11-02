console.log("author wang xiang 276644985@qq.com")

function getRootPath() {
    return "http://127.0.0.1:8080"
}

function getWSRootPath() {
    return "ws://127.0.0.1:8080/webSocket"
}

/**
 * 判断数字是否为正整数
 */
function validate(num) {
    if (num == null || num === "") {
        return false;
    }
    let reg = /^\d+(?=\.{0,1}\d+$|$)/
    return reg.test(num);
}