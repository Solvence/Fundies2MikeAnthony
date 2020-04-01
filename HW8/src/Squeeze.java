import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;


class SeamInfo {
  APixel thisPixel;
  double totalWeight;
  SeamInfo cameFrom;
  
  // the constructor
  SeamInfo(APixel thisPixel, double totalWeight, SeamInfo cameFrom) {
    this.thisPixel = thisPixel;
    this.totalWeight = totalWeight;
    this.cameFrom = cameFrom;
  }
  
  // constructor that only takes in two fields, used for the first row of pixels, when the seam has
  // nothing it came from
  SeamInfo(APixel thisPixel, double totalWeight) {
    this.thisPixel = thisPixel;
    this.totalWeight = totalWeight;
    this.cameFrom = null;
  }
}

class Picture extends World {
  APixel topLeft;
  int width;
  int height;
  SeamInfo seamToRemove;  // the seam to remove in the current tick. If there is no seam to remove  
  // currently, represents a default seam of the top left pixel, 0 weight, and null cameFrom
  
  Picture(String imgFileName) {
    FromFileImage img = new FromFileImage(imgFileName);
    this.width = (int) img.getWidth();
    this.height = (int) img.getHeight();
    topLeft = new SentinelPixel();
    APixel prevRowPixel = topLeft;
    for (int row = 0; row < img.getHeight(); row += 1) {
      APixel newPrevRowPixel = new SentinelPixel();
      newPrevRowPixel.up = prevRowPixel;
      newPrevRowPixel.down = prevRowPixel.down;
      newPrevRowPixel.up.down = newPrevRowPixel;
      newPrevRowPixel.down.up = newPrevRowPixel;
      prevRowPixel = newPrevRowPixel;
      APixel prevPixel = prevRowPixel;
      for (int col = 0; col < img.getWidth(); col += 1) {
        Pixel thisPixel = new Pixel(prevPixel.up.right, new SentinelPixel(), prevPixel, 
            prevPixel.right, img.getColorAt(col, row));
        if (row == 0) {
          SentinelPixel newSent = new SentinelPixel();
          newSent.left = prevPixel.up;
          newSent.right = prevPixel.up.right;
          prevPixel.up.right = newSent;
          newSent.right.left = newSent;
          thisPixel.up = newSent;
          thisPixel.down = newSent;
        } else {
          thisPixel.up = prevPixel.up.right;
          thisPixel.down = thisPixel.up.down;
        }
        thisPixel.up.down = thisPixel;
        thisPixel.down.up = thisPixel;
        thisPixel.left.right = thisPixel;
        thisPixel.right.left = thisPixel;
        prevPixel = thisPixel;
      }
    }
  }
  
  public WorldScene makeScene() {
    WorldImage img = new FromFileImage("balloons.jpg");
    ComputedPixelImage cpi = new ComputedPixelImage((int) img.getWidth(), (int) img.getHeight());
    APixel nextRowPixel = topLeft;
    for (int row = 0; row < img.getHeight(); row += 1) {
      nextRowPixel = nextRowPixel.down;
      APixel nextPixel = nextRowPixel.right;
      for (int col = 0; col < img.getWidth(); col += 1) {
        cpi.setPixel(col, row, nextPixel.color);
        nextPixel = nextPixel.right;
      }
    }
    
    WorldScene ws = new WorldScene(800, 344);
    ws.placeImageXY(cpi, 400, 172);
    
    return ws;
  }
  
  public void onTick() {
    if (this.seamToRemove != null && this.seamToRemove.cameFrom != null) {
      APixel currentPixel = this.seamToRemove.thisPixel;
      currentPixel.down.remove();
      while (this.seamToRemove.cameFrom != null) {
        currentPixel = this.seamToRemove.thisPixel;
        currentPixel.remove();
        this.seamToRemove = this.seamToRemove.cameFrom;
      }
    } else {
      ArrayList<ArrayList<SeamInfo>> seams = new ArrayList<ArrayList<SeamInfo>>();
      
      APixel nextRowPixel = topLeft;
      for (int row = 0; row < this.height; row += 1) {
        nextRowPixel = nextRowPixel.down;
        APixel nextPixel = nextRowPixel.right;
        seams.add(new ArrayList<SeamInfo>());
        for (int col = 0; col < this.width; col += 1) {
          if (row == 0) {
            seams.get(row).add(new SeamInfo(nextPixel, nextPixel.calculateEnergy()));
          } else {
            SeamInfo cameFrom = seams.get(row - 1).get(col);
            if (col != 0 && seams.get(row - 1).get(col - 1).totalWeight < cameFrom.totalWeight) {
              cameFrom = seams.get(row - 1).get(col - 1);
            }
            if (col != width - 1 
                && seams.get(row - 1).get(col + 1).totalWeight < cameFrom.totalWeight) {
              cameFrom = seams.get(row - 1).get(col + 1);
            }
            
            seams.get(row).add(new SeamInfo(nextPixel, nextPixel.calculateEnergy(), cameFrom));
          }
          nextPixel = nextPixel.right;
        }
      }
      SeamInfo seamToHighlight = seams.get(height - 1).get(0);
      
      for (int col = 1; col < this.width; col += 1) {
        if (seams.get(height - 1).get(col).totalWeight < seamToHighlight.totalWeight) {
          seamToHighlight = seams.get(height - 1).get(col);
        }
      }
      
      this.seamToRemove = seamToHighlight;
      
      while (seamToHighlight.cameFrom != null) {
        seamToHighlight.thisPixel.highlight();
        seamToHighlight = seamToHighlight.cameFrom;
      }
    }
  }
}

abstract class APixel {
  APixel up;
  APixel down;
  APixel left;
  APixel right;
  Color color;
  
  APixel(APixel up, APixel down, APixel left, APixel right, Color color) {
    this.up = up;
    this.down = down;
    this.left = left;
    this.right = right;
    this.color = color;
  }
  
  APixel() {
    this.up = null;
    this.down = null;
    this.left = null;
    this.right = null;
    this.color = Color.BLACK;
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
  
  void remove() {
    this.up.down = this.down;
    this.down.up = this.up;
    this.left.right = this.right;
    this.right.left = this.left;
  }
  
  void highlight() {
    this.color = Color.RED;
  }
}

class SentinelPixel extends APixel {
  SentinelPixel(Pixel up, Pixel down, Pixel left, Pixel right) {
    super(up, down, left, right, Color.BLACK);
  }
  
  SentinelPixel() {
    super();
    this.up = this;
    this.down = this;
    this.left = this;
    this.right = this;
    
    this.color = Color.BLACK;
  }
}

class Pixel extends APixel {
  
  Pixel(APixel up, APixel down, APixel left, APixel right, Color color) {
    super(up, down, left, right, color);
  }
  
  
  
  
}

class ExamplesSqueeze {
  
  // TODO test field of field on pixel neighbors
  
  void testWorld (Tester t) {
    
    Picture p = new Picture("balloons.jpg");
    p.bigBang(800, 344, 1.0 / 4);
        
    
  }
  
}