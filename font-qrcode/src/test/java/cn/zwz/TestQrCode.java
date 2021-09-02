package cn.zwz;

import org.junit.Test;

public class TestQrCode {
    @Test
    public void testQRCodeUtils() throws Exception {

        // 存放在二维码中的内容
        String text = "https://blog.csdn.net/qq_41464123?id=1"; // https://blog.csdn.net/qq_41464123
        // 嵌入二维码的图片路径
        String imgPath = "src/main/resources/logo.png";
        // 生成的二维码的路径及名称
        String destPath = "d://zwz.png";
        //生成二维码
        QRCodeUtil.encode(text, imgPath, destPath, true);
        // 解析二维码
        String str = QRCodeUtil.decode(destPath);
        // 打印出解析出的内容
        System.out.println(str);
    }
}
