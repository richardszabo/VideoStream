package hu.rics.videostreamreceiverdesktop;
 
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.net.ServerSocket;
import java.net.Socket;
 
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
 
/**
 * Based on this: https://lejosnews.wordpress.com/2014/09/04/webcam-streaming/
 * but imageformat is YUV422 with 2 bytes for each pixel
 * while YUV420sp is 1.5 bytes for each pixel.
 * Using decodeYUV420SP from here: //http://www.41post.com/3470/programming/android-retrieving-the-camera-preview-as-a-pixel-array 
 * 
 * @author rics
 */
public class VideoStreamReceiverDesktop {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int NUM_PIXELS = WIDTH * HEIGHT;
    private static final int BUFFER_SIZE = NUM_PIXELS * 3/2;
    private static final int PORT = 55555;
 
    private ServerSocket ss;
    private Socket sock;
    private byte[] buffer = new byte[BUFFER_SIZE];
    private int[] pixels = new int[NUM_PIXELS];
    private BufferedInputStream bis;
    private BufferedImage image;
    private CameraPanel panel = new CameraPanel();
    private JFrame frame;
 
    public VideoStreamReceiverDesktop() {  
        try {
            ss = new ServerSocket(PORT);
            System.out.println("before accept");
            sock = ss.accept();
            System.out.println("after accept");
            bis = new BufferedInputStream(sock.getInputStream());
        } catch (Exception e) {
            System.err.println("Failed to connect: " + e);
            System.exit(1);
        }
 
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    }
 
    public void createAndShowGUI() {
        frame = new JFrame("EV3 Camera View");
 
        frame.getContentPane().add(panel);
        frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
 
        frame.pack();
        frame.setVisible(true);
    }
 
    public void close() {
        try {
            if (bis != null) bis.close();
            if (sock != null) sock.close();
            if (ss != null) ss.close();
        } catch (Exception e1) {
            System.err.println("Exception closing window: " + e1);
        }
    }
 
    // convertYUVtoARGB is taken from here:
    // https://en.wikipedia.org/wiki/YUV#Y.E2.80.B2UV420sp_.28NV21.29_to_RGB_conversion_.28Android.29
    private int convertYUVtoARGB(int y, int u, int v) {
        // converting to unsigned int
        y = 0xFF & y;
        u = 0xFF & u;        
        v = 0xFF & v;        
        int r = (int)(y + (1.370705 * (v-128)));
        int g = (int)(y - (0.698001 * (v-128)) - (0.337633 * (u-128)));
        int b = (int)(y + (1.732446 * (u-128)));
        r = r>255? 255 : r<0 ? 0 : r;
        g = g>255? 255 : g<0 ? 0 : g;
        b = b>255? 255 : b<0 ? 0 : b;
        return 0xff000000 | (r<<16) | (g<<8) | b;
    }
    
/*private int convertYUVtoARGB(int y, int u, int v) {
        int c = y&0xFF - 16;
        int d = u&0xFF - 128;
        int e = v&0xFF - 128;
        int r = (298*c+409*e+128)/256;
        int g = (298*c-100*d-208*e+128)/256;
        int b = (298*c+516*d+128)/256;
        r = r>255? 255 : r<0 ? 0 : r;
        g = g>255? 255 : g<0 ? 0 : g;
        b = b>255? 255 : b<0 ? 0 : b;
        return 0xff000000 | (r<<16) | (g<<8) | b;        
    }*/

    public void run() {
        int i = 0;
        while(true) {
            synchronized (this) {
                try {
                    int offset = 0;
                    while (offset < BUFFER_SIZE) {
                        offset += bis.read(buffer, offset, BUFFER_SIZE - offset);
                    }
                    decodeYUV420SP(image,buffer,WIDTH,HEIGHT);
                } catch (Exception e) {
                    break;
                }
            }
            panel.repaint(1);
        }
    }
        
    // based on 
    // https://en.wikipedia.org/wiki/YUV#Y.E2.80.B2UV420p_.28and_Y.E2.80.B2V12_or_YV12.29_to_RGB888_conversion
    // YUV420SP aka NV21 pixel layout per channel is the following (for a 16 pixel image): 
    // 0123456789ABCDEF01234567
    // YYYYYYYYYYYYYYYYVUVUVUVU
    void decodeYUV420SP(BufferedImage image, byte[] yuv420sp, int width, int height) {
        for (int i = 0; i < height; ++i ) {
            for( int j = 0; j < width; ++j) {
                int y = yuv420sp[i * width + j];
                int v = yuv420sp[2*((i / 2) * (width/2) + (j/2)) + width*height];
                int u = yuv420sp[2*((i / 2) * (width/2) + (j/2))+1 + width*height];
                image.setRGB(j,i,convertYUVtoARGB(y, u, v));                
            }
        }    
    }
    
    //http://www.41post.com/3470/programming/android-retrieving-the-camera-preview-as-a-pixel-array  
    //Method from Ketai project! Not mine! See below...  
    // ugly and working
    /*void decodeYUV420SP(BufferedImage image, byte[] yuv420sp, int width, int height) {

        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) {
                    y = 0;
                }
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) {
                    r = 0;
                } else if (r > 262143) {
                    r = 262143;
                }
                if (g < 0) {
                    g = 0;
                } else if (g > 262143) {
                    g = 262143;
                }
                if (b < 0) {
                    b = 0;
                } else if (b > 262143) {
                    b = 262143;
                }

                image.setRGB(yp%WIDTH,yp/WIDTH,0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff));
            }
        }
    }*/

    class CameraPanel extends JPanel {
        private static final long serialVersionUID = 1L;
 
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Ensure that we don't paint while the image is being refreshed
            synchronized(VideoStreamReceiverDesktop.this) {
                g.drawImage(image, 0, 0, null);
            }
        }   
    }
 
    public static void main(String[] args) {    
        final VideoStreamReceiverDesktop videoStreamDesktop = new VideoStreamReceiverDesktop();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                videoStreamDesktop.createAndShowGUI(); 
            }
        });
        videoStreamDesktop.run();
    }
}