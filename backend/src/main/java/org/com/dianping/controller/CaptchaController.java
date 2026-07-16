package org.com.dianping.controller;

import javax.imageio.ImageIO;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

@RestController
public class CaptchaController {
    @GetMapping("/captcha")
    public void getCaptcha(HttpSession session, HttpServletResponse response) throws IOException {
        int width = 100, height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, width - 1, height - 1);

        Random rand;  // SecureRandom 优先于Random
        try {
            rand = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        int number = 1000 + rand.nextInt(9000);
        String captchaCode = String.valueOf(number);
        session.setAttribute("CAPTCHA_CODE", captchaCode);

        g.setColor(Color.BLUE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString(captchaCode, 10, 28);
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
            int x1 = rand.nextInt(width), y1 = rand.nextInt(height);
            int x2 = rand.nextInt(width), y2 = rand.nextInt(height);
            g.drawLine(x1, y1, x2, y2);
        }
        g.dispose();

        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        ImageIO.write(image, "png", response.getOutputStream());
    }
}
