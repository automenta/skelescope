/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package skelescope;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioBuffer;
import com.jme3.audio.AudioKey;
import com.jme3.audio.AudioNode;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
import com.jme3.system.AppSettings;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Skelescope extends SimpleApplication {
    
    public static class MotionState {
        public float points[][];
        public Matrix3f[] rotation;
        
        public MotionState(int vertices) {
             points = new float[vertices][3];
             rotation = new Matrix3f[vertices];
        }
    }
    
    int numBodyVertices;
    
    public final List<MotionState> motion = new ArrayList();
    
    public static void main(String[] args){
        AppSettings settings = new AppSettings(true);
        settings.setUseInput(false);
        settings.setVSync(true);
         
        Skelescope app = new Skelescope();
        app.setShowSettings(false);
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
                                    if (header[i].endsWith("(X)") && !header[i].startsWith("Floor"))
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
                                    m.rotation[i] = new Matrix3f(
                                            Float.parseFloat(s[pi+3]),
                                            Float.parseFloat(s[pi+4]),
                                            Float.parseFloat(s[pi+5]),
                                            Float.parseFloat(s[pi+6]),
                                            Float.parseFloat(s[pi+7]),
                                            Float.parseFloat(s[pi+8]),
                                            Float.parseFloat(s[pi+9]),
                                            Float.parseFloat(s[pi+10]),
                                            Float.parseFloat(s[pi+11])
                                    );
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
    int[][] edges = {{0,1},{1,2},{1,3},{3,4},{4,5},{1,6},{6,7},{7,8},{3,9},{9,10},{10,11},{6,12},{9,12},{12,13},{13,14},{2,12},{2,9}};
    Line[] edgeLines = new Line[edges.length];
 
    protected void setMotionIndex(float t) {
        float scale = 2.0f;
        
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
                    scale * (bc[0] * p + ac[0] * (1-p)), 
                    scale * (bc[1] * p + ac[1] * (1-p)),
                    scale * (bc[2] * p + ac[2] * (1-p)));
            g.setLocalRotation(motion.get(a).rotation[i]);
       }
        for (int i = 0; i < edgeLines.length; i++) {
            Vector3f from = bodyVertices[edges[i][0]].getLocalTranslation();
            Vector3f to = bodyVertices[edges[i][1]].getLocalTranslation();
            edgeLines[i].updatePoints(from, to);
        }

    }
    
    @Override
    public void simpleInitApp() {
     /*   PointLight lamp_light = new PointLight();
        lamp_light.setColor(ColorRGBA.Yellow);
        lamp_light.setRadius(4f);
        lamp_light.setPosition(new Vector3f(-.5f,-.5f,-.5f));
        rootNode.addLight(lamp_light);*/

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(1,0,-1).normalizeLocal());
        sun.setColor(ColorRGBA.White);   
        rootNode.addLight(sun);
    
        bodyVertices = new Geometry[numBodyVertices];

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        //mat.setBoolean("UseAlpha", true);
        //mat.setColor("Color", ColorRGBA.White);
        mat.setColor("Diffuse", new ColorRGBA(1f,1f,1f,0.3f));
        mat.setColor("Specular",new ColorRGBA(1f,1f,1f,0.3f));
        mat.setFloat("Shininess", 2f);  // [0,128]
        mat.setTransparent(true);
        
            
        for (int i = 0; i < numBodyVertices; i++) {
            /** this blue box is our player character */
            float radius = (i == 0) ? 0.15f : 0.07f;
            
            Box b = new Box(radius, radius, radius);
            Geometry g = new Geometry("b" + i, b);
            
            g.setMaterial(mat);
            
            rootNode.attachChild(g);
            
            bodyVertices[i] = g;            
        }
        
        
        for (int e = 0; e < edges.length; e++) {
            int from = edges[e][0];
            int to = edges[e][1];
            Line l = new Line(new Vector3f(), new Vector3f());
            l.setLineWidth(7);
            
            edgeLines[e] = l;
            Geometry g = new Geometry("l" + from + "_" + to, l);
            g.setMaterial(mat);
            rootNode.attachChild(g);

            edgeLines[e] = l;
        }
        
        
        ByteBuffer bb = ByteBuffer.allocateDirect(44100*2);
        for (int i = 0; i < 44100; i++) {
            short s = (short) ((Math.sin( ((double)i)/50.0 )*50));
            bb.putShort(s);
        }
                
        AudioBuffer audioBuffer = new AudioBuffer();
        audioBuffer.setupFormat(1, 16, 44100);
        audioBuffer.updateData(bb);

        System.out.println(audioBuffer.getDuration());
        
        AudioNode audioNode = new AudioNode(audioBuffer, new AudioKey("a1"));
        audioNode.setLooping(true);
        audioNode.setVolume(0.5f);
        audioNode.play();
        
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
