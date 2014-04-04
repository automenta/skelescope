/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package skelescope;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Skelescope extends SimpleApplication {
    
    public static class MotionState {
        public float points[][];
        
        public MotionState(int vertices) {
             points = new float[vertices][3];
        }
    }
    
    int numBodyVertices;
    
    public final List<MotionState> motion = new ArrayList();
    
    public static void main(String[] args){
        AppSettings settings = new AppSettings(true);
        //settings.setUseInput(false);
        settings.setVSync(true);
         
        Skelescope app = new Skelescope();
        //app.setShowSettings(false);
        app.setDisplayStatView(false);
        app.setDisplayFps(false);
        app.setSettings(settings);
        app.start();
    }

    public Skelescope() {
        super();
        loadMotion();
    }

    
    public void loadMotion() {
            System.out.println("loading");
            String csvFile = "data/sample_punches2.csv";
            BufferedReader br = null;
            String line = "";
            String cvsSplitBy = ",";

            String[] header;
            List<Integer> pointIndexes = new LinkedList();
            
            try {
                    int lineNum = 0; 
                    br = new BufferedReader(new FileReader(csvFile));
                    while ((line = br.readLine()) != null) {
                            if (lineNum++ < 8) continue; //skip header
                            
                            // use comma as separator
                            String[] s = line.split(cvsSplitBy);

                            if (lineNum == 9) {
                                header = s;
                                System.out.println(Arrays.asList(header));
                                for (int i = 0; i < header.length; i++) {
                                    if (header[i].endsWith("(X)"))
                                        pointIndexes.add(i);
                                }
                                numBodyVertices = pointIndexes.size();
                            }
                            else {
                                //System.out.println(s);
                                MotionState m = new MotionState(numBodyVertices);
                                for (int i = 0; i < pointIndexes.size(); i++) {
                                    int pi = pointIndexes.get(i);
                                    m.points[i][0] = Float.parseFloat(s[pi]);   //x
                                    m.points[i][1] = Float.parseFloat(s[pi+1]);  //y
                                    m.points[i][2] = Float.parseFloat(s[pi+2]);   //z
                                }
                                motion.add(m);
                            }
                            
                    }

            } catch (FileNotFoundException e) {
                    e.printStackTrace();
            } catch (IOException e) {
                    e.printStackTrace();
            } finally {
                    if (br != null) {
                            try {
                                    br.close();
                            } catch (IOException e) {
                                    e.printStackTrace();
                            }
                    }
            }

    }

    protected Geometry[] bodyVertices = null;
 
    protected void setMotionIndex(float t) {
        int a = (int)Math.floor(t);
        if (a >= motion.size()) a = motion.size()-1;
        int b = (int)Math.ceil(t);
        if (b >= motion.size()) b = motion.size()-1;
        
        float p = t - a;
        
        for (int i = 0; i < numBodyVertices; i++) {
           Geometry g = bodyVertices[i];
           float[] ac = motion.get(a).points[i];           
           float[] bc = motion.get(b).points[i];
           
            g.setLocalTranslation(
                    bc[0] * p + ac[0] * (1-p), 
                    bc[1] * p + ac[1] * (1-p),
                    bc[2] * p + ac[2] * (1-p));
       }

    }
    
    @Override
    public void simpleInitApp() {

        bodyVertices = new Geometry[numBodyVertices];
        for (int i = 0; i < numBodyVertices; i++) {
            /** this blue box is our player character */
            Box b = new Box(0.05f, 0.05f, 0.05f);
            Geometry g;
            g = new Geometry("b" + i, b);
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.White);
            g.setMaterial(mat);
            
            rootNode.attachChild(g);
            
            bodyVertices[i] = g;            
        }
        setMotionIndex(0);
        
    }
 
    float time = 0;
    /* Use the main event loop to trigger repeating actions. */
    @Override
    public void simpleUpdate(float tpf) {
        // make the player rotate:
        //player.rotate(0, 2*tpf, 0); 
        
        
        float mi = ( time * 9.0f );
        if (mi >= motion.size()) time = 0;
        setMotionIndex(mi);
        
        time += tpf;
    }

    @Override
    public void destroy() {
        try {
            super.destroy(); //To change body of generated methods, choose Tools | Templates.
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
}
