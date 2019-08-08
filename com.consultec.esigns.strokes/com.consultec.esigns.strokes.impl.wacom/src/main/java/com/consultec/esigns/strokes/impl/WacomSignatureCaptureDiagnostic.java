package com.consultec.esigns.strokes.impl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.consultec.esigns.strokes.api.IStrokeDiagnostic;
import com.florentis.signature.DynamicCapture;
import com.florentis.signature.SigCtl;
import com.florentis.signature.SigObj;

public class WacomSignatureCaptureDiagnostic extends JFrame implements IStrokeDiagnostic {

  private static final long serialVersionUID = 1L;

  private JPanel drawPanel;

  private JTextArea textArea;

  private BufferedImage signatureImage;

  public WacomSignatureCaptureDiagnostic() {

    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setTitle("Prueba diagnostico de dispositivo de Firma");
    this.setSize(new Dimension(450, 350));
    this.setLayout(new BorderLayout());

    drawPanel = new JPanel() {

      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      @Override
      public void paintComponent(Graphics g) {

        super.paintComponent(g);
        if (signatureImage != null) {
          g.drawImage(signatureImage, 0, 0, null);
        }
      }

    };

    drawPanel.setBackground(Color.WHITE);
    drawPanel.setPreferredSize(new Dimension(200, 150));

    JPanel panelImage = new JPanel();
    panelImage.add(drawPanel);

    this.add(panelImage, BorderLayout.WEST);

    JButton btnSign = new JButton("Sign");

    btnSign.setPreferredSize(new Dimension(100, 50));

    btnSign.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        textArea.append("btnSign was pressed\n");
        test();

      }

    });

    JPanel panelButton = new JPanel();
    panelButton.add(btnSign);

    this.add(panelButton, BorderLayout.EAST);

    textArea = new JTextArea(8, 20);
    textArea.setEditable(false);
    this.add(new JScrollPane(textArea), BorderLayout.SOUTH);

    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    this.setLocation(dim.width / 2 - this.getSize().width / 2,
      dim.height / 2 - this.getSize().height / 2);

    // this.showOnScreen(this);

  }

  void showOnScreen(Frame frame) {

    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gd = ge.getScreenDevices();

    int screen = 1;

    if (screen > -1 && screen < gd.length) {

      frame.setLocation(gd[screen].getDefaultConfiguration().getBounds().x,
        gd[screen].getDefaultConfiguration().getBounds().y + frame.getY());

    } else if (gd.length > 0) {

      frame.setLocation(gd[0].getDefaultConfiguration().getBounds().x,
        gd[0].getDefaultConfiguration().getBounds().y + frame.getY());

    } else {

      throw new RuntimeException("No Screens Found");

    }

  }

  public boolean test() {

    boolean retValue = true;

    SigCtl sigCtl;

    try {

      sigCtl = new SigCtl();
      sigCtl.licence("AgAkAMlv5nGdAQVXYWNvbQ1TaWduYXR1cmUgU0RLAgOBAgJkAACIAwEDZQA");
      sigCtl.aboutBox();

      DynamicCapture dc = new DynamicCapture();
      int rc = dc.capture(sigCtl, "who", "why", null, null);

      if (rc == 0) {

        textArea.append("signature captured successfully\n");
        String fileName = "sig1.png";
        SigObj sig = sigCtl.signature();

        sig.extraData("AdditionalData", "CaptureImage.java Additional Data");

        int flags = SigObj.outputFilename | SigObj.color32BPP | SigObj.backgroundTransparent
            | SigObj.encodeData;

        sig.renderBitmap(fileName, 200, 150, "image/png", 1.0f, 0xff0000, 0xffffff, 0.0f, 0.0f,
          flags);

        paintSignature(fileName);

      } else {

        textArea.append("signature capture error res=" + rc + "\n");

        switch (rc) {

          case 1:

            textArea.append("Cancelled\n");
            break;

          case 100:

            textArea.append("Signature tablet not found\n");
            break;

          case 103:

            textArea.append("Capture not licensed\n");
            break;

          default: {

            textArea.append("Unexpected error code\n");
            retValue = false;

          }

        }

      }

    } catch (Error | Exception ex) {

      textArea.append("Error " + ex.getMessage() + "\n");
      retValue = false;

    }


    return retValue;

  }

  private void paintSignature(String fileName) {

    try {

      signatureImage = ImageIO.read(new File("./" + fileName));
      drawPanel.repaint();

    } catch (IOException e) {

      System.out.println(e.toString());

    }

  }

  public static void main(String args[]) {

    WacomSignatureCaptureDiagnostic frame = new WacomSignatureCaptureDiagnostic();
    frame.setVisible(true);

  }

}
