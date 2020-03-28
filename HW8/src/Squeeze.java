import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;


class SeamInfo {
  
  Pixel thisPixel;
  double totalWeight;
  SeamInfo cameFrom;
  
  
}

class Picture extends World {

  @Override
  public WorldScene makeScene() {
    // TODO Auto-generated method stub
    WorldImage img = new FromFileImage("balloons.jpg");
    
    WorldScene ws = new WorldScene(800, 344);
    ws.placeImageXY(img, 400, 172);
    
    return ws;
  }
  
  
  
}


class Pixel {
  
  Pixel up;
  Pixel down;
  Pixel left;
  Pixel right;
  Color color;
  
  public Pixel(Pixel up, Pixel down, Pixel left, Pixel right, Color color) {
    this.up = up;
    this.down = down;
    this.left = left;
    this.right = right;
    this.color = color;
    
  }
  
  double calculateBrightness() {
    return ((double) (color.getRed() + color.getGreen() + color.getBlue())) / 3 / 255.0;
  }
  
  double calculateHorizEnergy() {
    
    return (this.up.left.calculateBrightness() +
        2 * this.left.calculateBrightness() +
        this.down.left.calculateBrightness()) - (
        this.up.right.calculateBrightness() +
        2 * this.right.calculateBrightness() +
        this.down.right.calculateBrightness());
        
        
  }
  
  double calculateVertEnergy() {
    
    return (this.up.left.calculateBrightness() +
        2 * this.up.calculateBrightness() +
        this.up.right.calculateBrightness()) - (
        this.down.left.calculateBrightness() +
        2 * this.down.calculateBrightness() +
        this.down.right.calculateBrightness());
        
  }
  
  double calculateEnergy() {
    return Math.sqrt(Math.pow(this.calculateHorizEnergy(), 2) +
        Math.pow(this.calculateVertEnergy(), 2));
  }
  
  
}


class ExamplesSqueeze {
  
  // TODO test field of field on pixel neighbors
  
  void testWorld (Tester t) {
    
    Picture p = new Picture();
    p.bigBang(800, 344);
        
    
  }
  
}