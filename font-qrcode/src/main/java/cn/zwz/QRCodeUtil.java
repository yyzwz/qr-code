package cn.zwz;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import sun.font.FontDesignMetrics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.util.Hashtable;

public class QRCodeUtil {
	private static final String CHARSET = "utf-8";
	private static final String FORMAT_NAME = "PNG";
	// 二维码尺寸
	private static final int QRCODE_SIZE = 300;
	// 二维码尺寸
	private static final int QRCODE_SIZE_HEIGHT = 420;
	// LOGO宽度
	private static final int WIDTH = 60;
	// LOGO高度
	private static final int HEIGHT = 60;
 
	
	private static BufferedImage createImage(String content, String imgPath, boolean needCompress) throws Exception {
		Hashtable hints = new Hashtable();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
		hints.put(EncodeHintType.MARGIN, 1);
		BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, QRCODE_SIZE, QRCODE_SIZE_HEIGHT, // 修改二维码底部高度
				hints);
		int width = bitMatrix.getWidth();
		int height = bitMatrix.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
			}
		}
		if (imgPath == null || "".equals(imgPath)) {
			return image;
		}
		// 插入图片
		QRCodeUtil.insertImage(image, imgPath, needCompress);
		// 插入底部文字
		QRCodeUtil.addFontImage(image, "1.进入时请主动出示此码和健康码",3);
		QRCodeUtil.addFontImage(image, "2.请自觉接受测温，佩戴口罩",2);
		QRCodeUtil.addFontImage(image, "3.此码当日有效，过期请重新预约",1);
		QRCodeUtil.addFontUp(image, "访客码");
		return image;
	}

	/**
	 * 添加 底部图片文字
	 *
	 * @param source      图片源
	 * @param declareText 文字本文
	 */
	private static void addFontImage(BufferedImage source, String declareText,int step) {
		BufferedImage textImage = strToImage(declareText, QRCODE_SIZE, 20,16);
		Graphics2D graph = source.createGraphics();
		//开启文字抗锯齿
		graph.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		int width = textImage.getWidth(null);
		int height = textImage.getHeight(null);

		Image src = textImage;
		graph.drawImage(src, 0, QRCODE_SIZE_HEIGHT - (20 * step) - 10, width, height, null);
		graph.dispose();
	}

	private static void addFontUp(BufferedImage source, String declareText) {
		BufferedImage textImage = strToImage(declareText, QRCODE_SIZE, 30,24);
		Graphics2D graph = source.createGraphics();
		//开启文字抗锯齿
		graph.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		int width = textImage.getWidth(null);
		int height = textImage.getHeight(null);

		Image src = textImage;
		graph.drawImage(src, 0, 30, width, height, null);
		graph.dispose();
	}

	@SuppressWarnings("restriction")
	private static BufferedImage strToImage(String str, int width, int height,int fontSize) {
		BufferedImage textImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D)textImage.getGraphics();
		//开启文字抗锯齿
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setBackground(Color.WHITE);
		g2.clearRect(0, 0, width, height);
		g2.setPaint(Color.BLACK);
		FontRenderContext context = g2.getFontRenderContext();
		Font font = new Font("微软雅黑", Font.BOLD, fontSize);
		g2.setFont(font);
		LineMetrics lineMetrics = font.getLineMetrics(str, context);
		FontMetrics fontMetrics = FontDesignMetrics.getMetrics(font);
		float offset = (width - fontMetrics.stringWidth(str)) / 2;
		float y = (height + lineMetrics.getAscent() - lineMetrics.getDescent() - lineMetrics.getLeading()) / 2;

		g2.drawString(str, (int)offset, (int)y);

		return textImage;
	}
 
	private static void insertImage(BufferedImage source, String imgPath, boolean needCompress) throws Exception {
		File file = new File(imgPath);
		if (!file.exists()) {
			System.err.println("" + imgPath + "   该文件不存在！");
			return;
		}
		Image src = ImageIO.read(new File(imgPath));
		int width = src.getWidth(null);
		int height = src.getHeight(null);
		if (needCompress) { // 压缩LOGO
			if (width > WIDTH) {
				width = WIDTH;
			}
			if (height > HEIGHT) {
				height = HEIGHT;
			}
			Image image = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics g = tag.getGraphics();
			g.drawImage(image, 0, 0, null); // 绘制缩小后的图
			g.dispose();
			src = image;
		}
		// 插入LOGO
		Graphics2D graph = source.createGraphics();
		int x = (QRCODE_SIZE - width) / 2;
		int y = (QRCODE_SIZE - height) / 2 + 60;
		graph.drawImage(src, x, y, width, height, null);
		Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
		graph.setStroke(new BasicStroke(3f));
		graph.draw(shape);
		graph.dispose();
	}
 
	/*
	 * 生成二维码
	 */
	public static void encode(String content, String imgPath, String destPath, boolean needCompress) throws Exception {
		BufferedImage image = QRCodeUtil.createImage(content, imgPath, needCompress);
		mkdirs(destPath);
		System.out.println(destPath);//user.dir指定了当前的路径 
		// String file = new Random().nextInt(99999999)+".jpg";
		// ImageIO.write(image, FORMAT_NAME, new File(destPath+"/"+file));
		ImageIO.write(image, FORMAT_NAME, new File(destPath));
	}
 
	public static BufferedImage encode(String content, String imgPath, boolean needCompress) throws Exception {
		BufferedImage image = QRCodeUtil.createImage(content, imgPath, needCompress);
		return image;
	}
 
	public static void mkdirs(String destPath) {
		File file = new File(destPath);
		// 当文件夹不存在时，mkdirs会自动创建多层目录，区别于mkdir．(mkdir如果父目录不存在则会抛出异常)
		if (!file.exists() && !file.isDirectory()) {
			file.mkdirs();
		}
	}
 
	public static void encode(String content, String imgPath, String destPath) throws Exception {
		QRCodeUtil.encode(content, imgPath, destPath, false);
	}
 
	public static void encode(String content, String destPath) throws Exception {
		QRCodeUtil.encode(content, null, destPath, false);
	}
 
	public static void encode(String content, String imgPath, OutputStream output, boolean needCompress)
			throws Exception {
		BufferedImage image = QRCodeUtil.createImage(content, imgPath, needCompress);
		ImageIO.write(image, FORMAT_NAME, output);
	}
 
	public static void encode(String content, OutputStream output) throws Exception {
		QRCodeUtil.encode(content, null, output, false);
	}
 
	/*
	 * 解析二维码
	 */
	public static String decode(File file) throws Exception {
		BufferedImage image;
		image = ImageIO.read(file);
		if (image == null) {
			return null;
		}
		BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		Result result;
		Hashtable hints = new Hashtable();
		hints.put(DecodeHintType.CHARACTER_SET, CHARSET);
		result = new MultiFormatReader().decode(bitmap, hints);
		String resultStr = result.getText();
		return resultStr;
	}
 
	public static String decode(String path) throws Exception {
		return QRCodeUtil.decode(new File(path));
	}
}
