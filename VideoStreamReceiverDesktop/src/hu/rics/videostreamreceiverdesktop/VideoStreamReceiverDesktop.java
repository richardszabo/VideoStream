package hu.rics.videostreamreceiverdesktop;
 
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.BoxLayout;
import javax.swing.JButton;
 
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
 
/**
 * Based on this: https://lejosnews.wordpress.com/2014/09/04/webcam-streaming/
 * but imageformat is YUV422 with 2 bytes for each pixel
 * while YUV420sp is 1.5 bytes for each pixel.
 * Using decodeYUV420SP from here: //http://www.41post.com/3470/programming/android-retrieving-the-camera-preview-as-a-pixel-array 
 * 
 * @author rics
 */
public class VideoStreamReceiverDesktop {
    private static final String DEFAULT_HOST = "192.168.0.101";
    private static final int PORT = 55556;
 
    private Socket socket;
    private int width;
    private int height;
    private int numPixels;
    private int bufferSize;
    
    private byte[] buffer; 
    private BufferedInputStream bis;
    private DataInputStream dis;
    private BufferedImage image;
    final private CameraPanel panel = new CameraPanel();
    private JFrame frame;
    private JTextField ipTextField;
    private JButton connectButton;
    boolean isConnected;
  
    private void createAndShowGUI() {
        frame = new JFrame("EV3 Camera View");
 
        ipTextField = new JTextField(DEFAULT_HOST);
        frame.setLayout(new BoxLayout(frame.getContentPane(),BoxLayout.Y_AXIS));
        frame.add(ipTextField);
        connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if( !isConnected ) {
                    new ImageUpdater().execute();
                } else {
                    close();
                }
            }
        });
        frame.add(connectButton);
        frame.add(panel);

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

    private void updateGUI() {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);   
        panel.setPreferredSize(new Dimension(width, height));        
        frame.setPreferredSize(new Dimension(width, height+connectButton.getHeight() + ipTextField.getHeight()));
        frame.pack();
    }
    
    private void close() {
        try {
            if (bis != null) bis.close();
            if (socket != null) socket.close();
            isConnected = false;
            System.out.println("Disconnected");            
            connectButton.setText("Connect");            
        } catch (Exception e1) {
            System.err.println("Exception closing window: " + e1);
        }
    }
     
    private boolean connect() {
        try {
            socket = new Socket(ipTextField.getText(),PORT);
            bis = new BufferedInputStream(socket.getInputStream());
            dis = new DataInputStream(bis);
            isConnected = true;
            System.out.println("Connected");
            connectButton.setText("Disconnect");
            return true;
        } catch (Exception e) {
            System.err.println("Failed to connect: " + e);
            return false;
        }         
    }
    
    private boolean getImageSize() {
        if( dis != null ) {
            try {
                width = dis.readInt();
                height = dis.readInt();
                System.out.println("width: " + width + " height: " + height);
                numPixels = width * height;
                bufferSize = numPixels * 3 / 2;
                buffer = new byte[bufferSize];                            
                return true;
            } catch (IOException ex) {
                System.err.println("Failed to get image size: " + ex);
            }
        }        
        return false;
    }
    
    private void receiveImages() {        
        while(isConnected) {
            synchronized (this) {
                try {
                    int offset = 0;
                    while (offset < bufferSize) {
                        offset += bis.read(buffer, offset, bufferSize - offset);
                    }
                    decodeYUV420SP(image,buffer,width,height);
                } catch (Exception e) {
                    break;
                }
            }
            panel.repaint(1);
        }
    }
    
   class ImageUpdater extends SwingWorker<Void, Void> {
       @Override
       public Void doInBackground() {
            if( connect() && getImageSize() ) {
                updateGUI();                    
                receiveImages();
            }
            return null;
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
            
    // based on 
    // https://en.wikipedia.org/wiki/YUV#Y.E2.80.B2UV420p_.28and_Y.E2.80.B2V12_or_YV12.29_to_RGB888_conversion
    // YUV420SP aka NV21 pixel layout per channel is the following (for a 16 pixel image): 
    // 0123456789ABCDEF01234567
    // YYYYYYYYYYYYYYYYVUVUVUVU
    private void decodeYUV420SP(BufferedImage image, byte[] yuv420sp, int width, int height) {
        for (int i = 0; i < height; ++i ) {
            for( int j = 0; j < width; ++j) {
                int y = yuv420sp[i * width + j];
                int v = yuv420sp[2*((i / 2) * (width/2) + (j/2)) + width*height];
                int u = yuv420sp[2*((i / 2) * (width/2) + (j/2))+1 + width*height];
                image.setRGB(j,i,convertYUVtoARGB(y, u, v));                
            }
        }    
    }
    
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
    }
}