package org.github.msx80.omicron;


import java.util.Random;

import org.github.msx80.omicron.api.Controller;
import org.github.msx80.omicron.api.Game;
import org.github.msx80.omicron.api.Mouse;
import org.github.msx80.omicron.api.SysConfig;
import org.github.msx80.omicron.api.Sys;
import org.github.msx80.omicron.basicutils.Colors;
import org.github.msx80.omicron.basicutils.TextDrawer;

public class HelloWorld implements Game {
	
	private Sys sys;
	private TextDrawer font = null;

	private int bgColor = Colors.from(150, 200, 255);
	Random r = new Random(10);
	
	int x=100;
	int y=10;
	int dir = 0;
	Mouse m;
	boolean oldClick = false;
	int newSurf;
	
    public void init(final Sys sys) 
    {
        this.sys = sys;
        font = new TextDrawer(sys, 1, 8, 8, 6);
        
        setupSurf(sys);
    }

	private void setupSurf(final Sys sys) {
		newSurf = sys.newSurface(64, 16);
		
        for (int x = 0; x < 64; x++) {
			for (int y = 0; y < 16; y++) {
				sys.setPix(newSurf, x, y, r.nextInt());
			}
		}
	}

	private void testRot(int x, int y) {
		font.print("Rot & flip:", x, y);
		y+=10;
		for (int i = 0; i < 4; i++) {
			sys.draw(2, x+i*18,y, 0,0, 16, 16, i, 0 );
			sys.draw(2, x+i*18,y+20, 0,0, 16, 16, i, 1 );
			sys.draw(2, x+i*18,y+40, 0,0, 16, 16, i, 2 );
			sys.draw(2, x+i*18,y+60, 0,0, 16, 16, i, 3 );
		}
		//sys.draw(2, 10,10, 32,32, 16, 16);
	}
    
    public void render() 
    {
    	sys.clear(bgColor);
    	
    	// keyboard controlled sprite
        sys.draw(2, x, y, 0, 32, 16, 16,0,dir);
        
    	
        font.print("Hello world!", 10, 10);
        
        sys.color(Colors.RED);
        font.print("FPS: "+sys.fps(), 10, 20);
        
        sys.color(Colors.WHITE);
        
        
        font.print("Mouse: "+m.x+" "+m.y+" "+m.btn[0], 10, 30);
              
        
        font.print("New surf:", 10, 50);
        sys.draw(newSurf, 10, 60, 0,0, 64, 16, 0, 0);
        
        
        testRot(140, 10);
    	
        testTrans(10,100);
        
        // mouse pointer
        sys.draw(2, m.x,m.y,64,0,8,8,0,0);
    }

    private void testTrans(int x, int y) {
    	font.print("Color & alpha:", x, y);
    	y+=10;
    	sys.color(Colors.WHITE);
    	sys.draw(2, x, y, 0, 16, 16, 16,0,0);
    	sys.color(Colors.RED);
    	sys.draw(2, x+16, y, 0, 16, 16, 16,0,0);
    	sys.color(Colors.GREEN);
    	sys.draw(2, x+32, y, 0, 16, 16, 16,0,0);
    	sys.color(Colors.BLUE);
    	sys.draw(2, x+48, y, 0, 16, 16, 16,0,0);
    	sys.color(Colors.from(255, 255, 255, 180));
    	sys.draw(2, x+64, y, 0, 16, 16, 16,0,0);
    	sys.color(Colors.from(255, 255, 255, 100));
    	sys.draw(2, x+80, y, 0, 16, 16, 16,0,0);
    	sys.color(Colors.from(255, 255, 255, 30));
    	sys.draw(2, x+96, y, 0, 16, 16, 16,0,0);
    	
    	
    	sys.color(Colors.WHITE);
		
	}

	public boolean update() {
			
        Controller c = sys.controllers()[0];
        if (c.up) y--;
        if (c.down) y++;
        if (c.left) { x--; dir  = 1; }
        if (c.right) { x++; dir = 0; }
        
        
        m = sys.mouse();
        if(m.btn[0] && !oldClick) {
        	sys.sound(1, 1f, r.nextFloat()*1.5f+0.5f);
        }
        oldClick = m.btn[0];
        
        return true;
    }

	@Override
	public SysConfig sysConfig() {
		return new SysConfig(240, 136, "Hello World!", "helloworld");
	}
  
}
