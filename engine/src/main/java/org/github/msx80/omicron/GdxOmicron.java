package org.github.msx80.omicron;


import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

import org.github.msx80.omicron.api.Controller;
import org.github.msx80.omicron.api.Game;
import org.github.msx80.omicron.api.Mouse;
import org.github.msx80.omicron.api.adv.AdvancedSys;
import org.github.msx80.omicron.basicutils.Colors;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;


public final class GdxOmicron extends ApplicationAdapter implements AdvancedSys {
	
	FPSLogger fps = new FPSLogger();
	SpriteBatch batch;
	OrthographicCamera cam=new OrthographicCamera();
	
	// a stack of all currently running Game
	Stack<GameRun> gameStack = new Stack<GameRun>();
	
	GameRun current; // top of the stack
	
	Mouse mouse = new Mouse();
	Controller[] controllers;
	
	
	private int ox = 0;
	private int oy = 0;
	
	Texture pixel;
	private int lastPixel;
		
	//Rectangle scissor = new Rectangle();

	
	
	
	Music currentMusic = null;
	private HardwareInterface hw;
	
	public GdxOmicron(Game game, HardwareInterface hw) {
		super();
		this.hw = hw;
		GameRun gr = new GameRun(game, new ScreenInfo(), null);
		this.gameStack.push(gr);
		current = gr;
	}
	
	Vector3 proj = new Vector3();
	private Preferences prefs = null;

	@Override
	public void create () {

		
		Pixmap p = new Pixmap(1, 1, Format.RGBA8888);
		p.drawPixel(0, 0, Colors.WHITE);
		pixel = new Texture(p);
		lastPixel = Colors.WHITE;
		
		
		batch = new SpriteBatch();
		//batch.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA,GL20.GL_SRC_ALPHA, GL20.GL_DST_ALPHA);
		//batch.enableBlending();
		// batch.setBlendFunction(GL20.GL_ONE_MINUS_DST_ALPHA, GL20.GL_DST_ALPHA);	
	
		controllers = new Controller[] {new Controller()}; // first is keyboard, TODO use joypad etc.
		
		Gdx.input.setInputProcessor(new MyInputProcessor());
		
		//Cursor cursor = Gdx.graphics.newCursor(new Pixmap(1, 1, Format.RGBA8888),0,0);
		//Gdx.graphics.setCursor(cursor);
		
		initOrResumeGameRun(current);
		
	}

	private void initOrResumeGameRun(GameRun r) {
		boolean isResume = r.screenInfo.requiredSysConfig != null;
		if(!isResume) r.screenInfo.requiredSysConfig = r.game.sysConfig();

		
		// for some reason, sounds are not played the first time on android, possibly becouse of asyncronous loading
		// and sounds not ready yet. As a super sketchy patch, we preload some sounds here (work sequentially, if there's an hole, whatever)
		if(Gdx.app.getType() == ApplicationType.Android) {
			for (int i = 1; true; i++) {
				Sound x = r.getSound(i);
				if(x==null)break;
			}
		}
		if(!isResume) 
		{
			r.game.init(this);
			for (HardwarePlugin hwp : r.plugins.values()) {
				hwp.init(this, hw);
			}
		}
		if(r.screenInfo.requiredSysConfig.title!=null) Gdx.graphics.setTitle(r.screenInfo.requiredSysConfig.title);
		setUpCam(r, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() );
	}

	private void setUpCam(GameRun r, int winwidth, int winheight) 
	{
		System.out.println("Resize to "+winwidth+" "+winheight);
		r.screenInfo.handleResize(winwidth, winheight, cam);
	}

	
	
	@Override
	public void resize(int width, int height) {
		
		setUpCam(current,width, height);
		
	}

	public void colorf(float red, float green, float blue, float alpha) {
		batch.setColor(red, green, blue, alpha);
	}

	

	
	@Override
	public void render () {
		fps.log();
		offset(0,0); // reset offset
			
		current.game.update(); 
		
		batch.setProjectionMatrix(cam.combined);
	
		this.colorf(1, 1, 1, 1);
		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
		this.clear(Colors.BLACK);
		Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
			
		current.screenInfo.applyGlClipping();
		
		batch.begin();
		try
		{
			current.game.render();
		}
		finally
		{
			batch.end();
		}
	}



	
	
	@Override
	public void dispose () {
		batch.dispose();
	}

	@Override
	public void clear(int value) {
		
		// todo cache the last clear() color to avoid recalculating all every time
		// it's usually jsut the same color every type.
		float r = ((value & 0xff000000) >>> 24) / 255f;
		float g = ((value & 0x00ff0000) >>> 16) / 255f;
		float b = ((value & 0x0000ff00) >>> 8) / 255f;

		Gdx.gl.glClearColor(r, g, b, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}


	private void draw(int sheetNum, int x, int y, int srcx, int srcy, int w, int h)
	{
		// uploadDirtyTexture();
		
		TextureRegion r = current.getSheet(sheetNum);
		if(r == null) throw new RuntimeException("Trying to draw sheet "+sheetNum+" but was not found");
		r.setRegion(srcx, srcy, w, h);
		r.flip(false, true);
		batch.draw(r, x+ox, y+oy);
		// batch.draw(r.getTexture(), x,y,srcx,srcy,w,h);
	}
	
	@Override
	public void draw(int sheetNum, int x, int y, int srcx, int srcy, int w, int h, int rotate, int flip)
	{
		current.uploadDirtyTexture();
		if(rotate == 0 && flip == 0)
		{
			draw(sheetNum, x, y, srcx, srcy, w, h);
		}
		else
		{
			/* 0 = No Flip
					1 = Flip horizontally
					2 = Flip vertically
					3 = Flip both vertically and horizontally
				*/	
			boolean flipx = flip == 1 || flip == 3; 
			boolean flipy = flip >= 2;
			
			int angle = rotate * 90;
			
			TextureRegion r = current.getSheet(sheetNum);
			batch.draw(r.getTexture(), x+ox, y+oy,w/2f,h/2f,w,h,1,1,angle, srcx, srcy, w, h, false != flipx, true != flipy);
		}
		
	}


	@Override
	public int getPix(int sheetNum, int x, int y) {
		if(sheetNum == 0)
		{
			boolean is = batch.isDrawing(); 
			if(is)
			{
				batch.end();
			}
			
			
			// not working yet
			proj.set(x,y,0);
			cam.project(proj);
			
			byte[] b = ScreenUtils.getFrameBufferPixels(Math.round(proj.x), Math.round(proj.y), 1, 1, false);
			
			if(is) batch.begin();
			
			return Colors.from(0xFF & b[0], 0xFF & b[1], 0xFF &  b[2], 0xFF & b[3]);
		}
		else
		{
			TextureRegion r = current.getSheet(sheetNum);
			PixmapTextureData d = (PixmapTextureData) r.getTexture().getTextureData();
			
			return d.consumePixmap().getPixel(x, y);
		}
	}

	@Override
	public int newSurface(int w, int h) {
		Pixmap p = new Pixmap(w, h, Format.RGBA8888);
		p.setBlending(Blending.None);
		
		int i = -1;
		while(current.sheets.containsKey(i))
		{
			i--;
		}
		
		Texture tt = new Texture(p, false);
		
		TextureRegion img = new TextureRegion(tt);
		img.flip(false, true);
		
		current.sheets.put(i, img);
		
		return i;
	}

	@Override
	public void fill(int sheetNum, int x, int y, int w, int h, int color) {
		if(sheetNum==0)
		{
			if(lastPixel != color)
			{
				boolean a = batch.isDrawing();
				if(a)batch.end();
				( (PixmapTextureData) pixel.getTextureData() ).consumePixmap().drawPixel(0, 0, color);
				lastPixel = color;
				pixel.load(pixel.getTextureData());
				if(a)batch.begin();
			}
			batch.draw(pixel, x+ox, y+oy, w, h);
		}
		else
		{
			TextureRegion r = current.getSheet(sheetNum);
			PixmapTextureData d = (PixmapTextureData) r.getTexture().getTextureData();
			Pixmap pp = d.consumePixmap();
			pp.setColor(color);
			
			pp.fillRectangle(x, y, w, h); // this works becouse we set blending to none
			
			current.addDirtyTexture(sheetNum);
		}
	}


	@Override
	public void offset(int x, int y) {
		ox = x;
		oy  = y;
		
	}
	
	
	public class MyInputProcessor implements InputProcessor {
		   public boolean keyDown (int keycode) {
			   Controller c = controllers[0];
			   switch (keycode) {
					case Input.Keys.UP: c.up=true; break;
					case Input.Keys.DOWN: c.down=true; break;
					case Input.Keys.LEFT: c.left=true; break;
					case Input.Keys.RIGHT: c.right=true; break;
		
					case Input.Keys.Z: c.btn[0]=true; break;
					case Input.Keys.X: c.btn[1]=true; break;
			
			default:
				break;
			}
		      return true;
		   }

		   public boolean keyUp (int keycode) {
			   Controller c = controllers[0];
			   switch (keycode) {
					case Input.Keys.UP: c.up=false; break;
					case Input.Keys.DOWN: c.down=false; break;
					case Input.Keys.LEFT: c.left=false; break;
					case Input.Keys.RIGHT: c.right=false; break;
		
					case Input.Keys.Z: c.btn[0]=false; break;
					case Input.Keys.X: c.btn[1]=false; break;
			
			default:
				break;
			}
			   return true;
		   }

		   public boolean keyTyped (char character) {
		      return true;
		   }

		   public boolean touchDown (int x, int y, int pointer, int button) {
			   if(mouse == null) return true;
			   proj.set(x,y,0);
			   cam.unproject(proj);
			   mouse.x = (int) proj.x;
			   mouse.y = (int) proj.y;
			  mouse.btn[0] = true;
		      return true;
		   }

		   public boolean touchUp (int x, int y, int pointer, int button) {
			   if(mouse == null) return true;
			   proj.set(x,y,0);
			   cam.unproject(proj);
			   mouse.x = (int) proj.x;
			   mouse.y = (int) proj.y;
			   mouse.btn[0] = false;
		      return true;
		   }

		   public boolean touchDragged (int x, int y, int pointer) {
			   if(mouse == null) return true;
			   proj.set(x,y,0);
			   cam.unproject(proj);
			   mouse.x = (int) proj.x;
			   mouse.y = (int) proj.y;
		      return true;
		   }

		   
		   public boolean mouseMoved (int x, int y) {
			   if(mouse == null) return true; 
			   proj.set(x,y,0);
			   cam.unproject(proj);
			   mouse.x = (int) proj.x;
			   mouse.y = (int) proj.y;
		      return true;
		   }

		   public boolean scrolled (int amount) {
		      return false;
		   }
		}


	@Override
	public int fps() {
		return Gdx.graphics.getFramesPerSecond();
	}

	
	static Color tmpColor = new Color(0);
	@Override
	public void color(int color) {
		Color.rgba8888ToColor(tmpColor, color);
		batch.setColor(tmpColor);
	}

	@Override
	public Mouse mouse() {
		return mouse;
	}

	@Override
	public Controller[] controllers() {
		return controllers;
	}

	@Override
	public void sound(int soundNum, float volume, float pitch) {
		Sound r = current.getSound(soundNum);
		if(r == null) throw new RuntimeException("Trying to play sound "+soundNum+" but was not found");
		r.play(volume, pitch, 0);
		
	}

	@Override
	public String mem(String key) {
		return getPrefs().getString(key);
		
	}

	private Preferences getPrefs() {
		if(prefs == null)
		{
			prefs = Gdx.app.getPreferences(current.screenInfo.requiredSysConfig.code);
		}
		return prefs ;
	}

	@Override
	public void mem(String key, String value) {
		
			getPrefs().putString(key, value).flush();
		
	}

	@Override
	public String hardware(String module, String command, String param) {
		HardwarePlugin e = current.plugins.get(module);
		return e == null ? null : e.exec(command, param);
	}

	@Override
	public void music(int musicNum, float volume, boolean loop) {
		if (currentMusic != null) {
			currentMusic.stop();
		}
		currentMusic = current.getMusic(musicNum);
		if(currentMusic == null) throw new RuntimeException("Trying to play music "+musicNum+" but was not found");
		currentMusic.setVolume(volume);
		currentMusic.setLooping(loop);
		currentMusic.play();
		
	}

	@Override
	public void stopMusic() {
		if (currentMusic != null) {
			currentMusic.stop();
			currentMusic = null;
		}
	}
	
	@Override
	public void execute(Game game, Consumer<String> onResult) {
		GameRun gr = new GameRun(game, new ScreenInfo(), onResult);
		gameStack.push(gr);
		current = gr;
		initOrResumeGameRun(gr);
		// we clean mouse state so that clicking doesn't pass to child
		for (int i = 0; i < mouse.btn.length; i++) {
			mouse.btn[i] = false;
		}
		// need to run an update() becouse startChildGame is called in the parent game's update(), so the child will lose a loop and get render() called first, which is wrong
		gr.game.update();
	}

	@Override
	public void quit(String result) {
		GameRun old = gameStack.pop();
		old.dispose();
		Consumer<String> x = old.onResult;
		current = gameStack.peek();
		
		// we clean mouse state so that clicking doesn't pass to parent
		for (int i = 0; i < mouse.btn.length; i++) {
			mouse.btn[i] = false;
		}
		
		initOrResumeGameRun(current);
		
		if(x!=null) x.accept(result);
		
		old = null;
		x = null;
		System.gc();
	}
	
}
