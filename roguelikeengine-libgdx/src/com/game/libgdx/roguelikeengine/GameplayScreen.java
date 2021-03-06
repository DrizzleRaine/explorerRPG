package com.game.libgdx.roguelikeengine;

/*
Copyright (C) 2013  Ferran Fabregas (ferri.fc@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;
import java.lang.Thread;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.game.libgdx.roguelikeengine.pathing.Path;
import com.game.libgdx.roguelikeengine.pathing.Pathing;
import com.game.libgdx.roguelikeengine.ui.ActionButton;
import com.game.libgdx.roguelikeengine.ui.BaseButton;
import com.game.libgdx.roguelikeengine.ui.ButtonAction;
import com.game.libgdx.roguelikeengine.ui.HBox;
import com.game.libgdx.roguelikeengine.ui.IButton;
import com.game.libgdx.roguelikeengine.ui.TextButton;






public class GameplayScreen extends InputAdapter implements Screen  {
	public static GameplayScreen instance = null;
	
	private SpriteBatch batch;
	// sound effects
	private Sound die;
	private Sound pickup;
	private Sound fight;
	private Sound drink;
	private Sound fireball;
	//private Texture texture;
	private BitmapFont genericfont;
	private BitmapFont messagefont;

	private Map[] maplayers = new Map[WrapperEngine.NUMBER_OF_MAP_LAYERS]; // new dynamic layer system
	private Map activemap;
	private Hero prota;
	private WrapperEngine game;
    private Layout layout; 
    
    // inventory status and modes
    int object_inv_mode=0;
    int object_drop_mode=0;
    int eye_mode=0;
    int debug_mode=0;
    int consumable_inv_mode=0;
    
    private LinkedList<Bullet> bullets = new LinkedList<Bullet>();
   
    
    protected boolean animateHeroInPlace = true;
    protected int animateHeroDelay = 3;
    protected int animateHeroDuration = 0;
    
    private int realXcoord;
    private int realYcoord;
    
    private boolean touchedLastFrame = false;
    
    Enemy actualenemy; // enemy that i'm over
    Buddy actualbuddy;
    Object actualobject; 
    Consumable actualconsumable;
    String interactionoutput="";
    ArrayList<Enemy> badguys;
    ArrayList<Buddy> goodguys;
    ArrayList<Object> availableobjects;
    ArrayList<Consumable> availableconsumables;
    
    Pathing<Hero> heroPathing;
    Path<Hero> lastPath;
    
    LinkedList<IButton> buttons = new LinkedList<IButton>();
    
    private Object_inventory objinv;
    Consumable_inventory consinv;
    
    private PopupInfoText screentext;
    
    // fight status
    int just_interact=0;
    
    
    private Rectangle viewport;
    private OrthographicCamera camera;
    
    public GameplayScreen() {

    	if(instance == null) instance = this;
    }
	
    @Override
    public void hide() {
    	
    }
    
	@Override
	public void show() {		
		init();
		
        placeboss();
		
	}

	/**
	 * 
	 */
	protected void placeboss() {
		// create final boss
        boolean boss_created=false;
		while (boss_created==false) {
			Random randomGenerator = new Random();
			int x = randomGenerator.nextInt(WrapperEngine.TOTAL_X_TILES);
			int y = randomGenerator.nextInt(WrapperEngine.TOTAL_Y_TILES);
			if (maplayers[0].gettiles()[x][y].isbloqued()) { // if there is empty space
				game.createenemy(0,"megaboss", 43, 46, 51, 310, x, y,"orc.png");
				boss_created=true;
			}
				
		}
	}

	/**
	 * 
	 */
	protected void init() {
		Gdx.input.setInputProcessor(this);
		batch = new SpriteBatch();

		float buttonWidth = Gdx.graphics.getWidth() * 0.1f;
		float buttonHeight = Gdx.graphics.getHeight() * 0.1f;
		if(WrapperEngine.OUTPUT_OS.equals("android")) {
			buttons.add(new TextButton("Go Up",    .75f, .2f, buttonWidth, buttonHeight));
			buttons.add(new TextButton("Go Down",  .75f, .1f, buttonWidth, buttonHeight));
			buttons.add(new TextButton("Go Left",  .65f, .15f, buttonWidth, buttonHeight));
			buttons.add(new TextButton("Go Right", .85f, .15f, buttonWidth, buttonHeight));
			
			getButton("Go Up").addActionListener(new ButtonAction() {
				@Override
				public void onClicked(IButton button) {
					goup();
				}
	
				@Override public void onMouseEnter(IButton button) {}
				@Override public void onMouseExit(IButton button) {}
			});
			getButton("Go Left").addActionListener(new ButtonAction() {
				@Override
				public void onClicked(IButton button) {
					goleft();
				}
	
				@Override public void onMouseEnter(IButton button) {}
				@Override public void onMouseExit(IButton button) {}
			});
			getButton("Go Right").addActionListener(new ButtonAction() {
				@Override
				public void onClicked(IButton button) {
					goright();
				}
	
				@Override public void onMouseEnter(IButton button) {}
				@Override public void onMouseExit(IButton button) {}
			});
			getButton("Go Down").addActionListener(new ButtonAction() {
				@Override
				public void onClicked(IButton button) {
					godown();
				}
	
				@Override public void onMouseEnter(IButton button) {}
				@Override public void onMouseExit(IButton button) {}
			});
			
			float leftButtonStartY = 0.1f;
			
			buttons.add(new TextButton("Magic", .05f, leftButtonStartY, buttonWidth, buttonHeight));
			buttons.add(new TextButton("Talk",  .05f, leftButtonStartY + (.1f * 1), buttonWidth, buttonHeight));
			buttons.add(new TextButton("Look",  .05f, leftButtonStartY + (.1f * 2), buttonWidth, buttonHeight));
			buttons.add(new TextButton("Drop",  .05f, leftButtonStartY + (.1f * 3), buttonWidth, buttonHeight));
			buttons.add(new TextButton("Take",  .05f, leftButtonStartY + (.1f * 4), buttonWidth, buttonHeight));
			buttons.add(new TextButton("Fight", .05f, leftButtonStartY + (.1f * 5), buttonWidth, buttonHeight));
			
			getButton("Fight").addActionListener(new ButtonAction() {
				@Override
				public void onClicked(IButton button) {
					fight();
				}
	
				@Override public void onMouseEnter(IButton button) {}
				@Override public void onMouseExit(IButton button) {}
			});
			getButton("Take").addActionListener(new ButtonAction() {
				@Override
				public void onClicked(IButton button) {
					take();
				}
	
				@Override public void onMouseEnter(IButton button) {}
				@Override public void onMouseExit(IButton button) {}
			});
			getButton("Drop").addActionListener(new ButtonAction() {
				@Override
				public void onClicked(IButton button) {
					drop();
				}
	
				@Override public void onMouseEnter(IButton button) {}
				@Override public void onMouseExit(IButton button) {}
			});
			getButton("Look").addActionListener(new ButtonAction() {
				@Override
				public void onClicked(IButton button) {
					look();
				}
	
				@Override public void onMouseEnter(IButton button) {}
				@Override public void onMouseExit(IButton button) {}
			});
			getButton("Talk").addActionListener(new ButtonAction() {
				@Override
				public void onClicked(IButton button) {
					talk();
				}
	
				@Override public void onMouseEnter(IButton button) {}
				@Override public void onMouseExit(IButton button) {}
			});
			getButton("Magic").addActionListener(new ButtonAction() {
				@Override
				public void onClicked(IButton button) {
					magic();
				}
	
				@Override public void onMouseEnter(IButton button) {}
				@Override public void onMouseExit(IButton button) {}
			});
		} else {
			float percentOfScreen = 0.65f;
			float actionBarWidth = (Gdx.graphics.getWidth() * percentOfScreen);
			
			buttonWidth = (Gdx.graphics.getWidth() * percentOfScreen) / 11;
			
			buttons.add(new HBox(0f, .99f, actionBarWidth)
				   		.addElement(new ActionButton(ActionButton.FIGHT, buttonWidth, buttonHeight))
				   		.addElement(new ActionButton(ActionButton.TAKE, buttonWidth, buttonHeight))
				   		.addElement(new ActionButton(ActionButton.DROP, buttonWidth, buttonHeight))
				   		.addElement(new ActionButton(ActionButton.LOOK, buttonWidth, buttonHeight))
				   		.addElement(new ActionButton(ActionButton.TALK, buttonWidth, buttonHeight))
				   		.addElement(new ActionButton(ActionButton.MAGIC, buttonWidth, buttonHeight))
				   		.addElement(new ActionButton(ActionButton.QUIT, buttonWidth, buttonHeight))
				   		.addElement(new ActionButton(ActionButton.LEFT, buttonWidth, buttonHeight))
				   		.addElement(new ActionButton(ActionButton.DOWN, buttonWidth, buttonHeight))
				   		.addElement(new ActionButton(ActionButton.UP, buttonWidth, buttonHeight))
				   		.addElement(new ActionButton(ActionButton.RIGHT, buttonWidth, buttonHeight))
				   		.finalizeHBox()
					);
		}
		
		// fonts setup
		FileHandle fontFile = Gdx.files.internal("fonts/diabloheavy.ttf");
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
		
		// set sound effects
		setupsoundeffects();
		
		//genericfont = new BitmapFont();
		//messagefont = new BitmapFont();
		messagefont = generator.generateFont(30); // px
    	messagefont.setColor(Color.YELLOW);
    	genericfont = generator.generateFont(14); // px
    	genericfont.setColor(Color.WHITE);
    	
    	// create a message info screen 
    	screentext=new PopupInfoText(100,(WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-450,"UI/text_popup.png",(int)(Gdx.graphics.getWidth() * 0.75f), (int) (Gdx.graphics.getHeight() * 0.4f));
    	screentext.settextoffset(50, 50);
    	
		// create tile layout
        game = new WrapperEngine();
        game.init();
        
        maplayers[0]=game.getmaplayer(0);
        maplayers[1]=game.getmaplayer(1);
        maplayers[2]=game.getmaplayer(2);
        
        layout=new Layout();
        prota = game.gethero();
        badguys= game.getenemies();
        goodguys= game.getbuddies();
        availableobjects=game.getobjects();
        availableconsumables=game.getconsumables();
        
        // empty enemy object that hold enemy. Same as object and consumable.
        actualbuddy= new Buddy();
        actualenemy= new Enemy();
        actualobject= new Object();
        actualconsumable= new Consumable();
        
        objinv = new Object_inventory();
        consinv= new Consumable_inventory();
		
        // create welcoming buddy
        final Buddy priest = game.createbuddy(0,"Priest", 19,24,"buddy1.png","Hi, my friend.\nI hope you enjoy your trip!\nBe aware of the monsters.\nDo you require healing?\n\t Accept_Healing \t No");
		
		screentext.addWordClickListener("Accept_Healing", new WordClickAction() {
			@Override
			public void onClicked(String word) {
				priest.healHero(prota, 10);
			}
		});
		
		screentext.addWordClickListener("No", new WordClickAction() {
			@Override
			public void onClicked(String word) {
				closeScreenText();
			}
		});
		
		screentext.addWordClickListener("treasure_chest!", new WordClickAction() {

			@Override
			public void onClicked(String word) {
				alert("Would you like to open it? \n \t Open_Chest \n \t Not_Right_Now");
			}
		});
		
		screentext.addWordClickListener("Open_Chest", new WordClickAction() {

			@Override
			public void onClicked(String word) {
				if(!Chest.interacting.open(game.getactivemap(), game.gethero())) {
					if(!game.gethero().hasKey()) alert("You need a key to open that!");
				};
			}
		});
		
		screentext.addWordClickListener("locked_door!", new WordClickAction() {

			@Override
			public void onClicked(String word) {
				alert("Would you like to open it? \n \t Open_Door \n \t Not_Right_Now");
			}
		});
		
		screentext.addWordClickListener("Open_Door", new WordClickAction() {

			@Override
			public void onClicked(String word) {
				if(!Chest.interacting.open(game.getactivemap(), game.gethero())) {
					if(!game.gethero().hasKey()) alert("You need a key to open that!");
				};
			}
		});
		
		screentext.addWordClickListener("Not_Right_Now", new WordClickAction() {

			@Override
			public void onClicked(String word) {
				closeScreenText();
			}
		});
	}
	
	public Buddy createpriest(int column, int row, String dialog) {
		Buddy priest = game.createbuddy(0,"Priest", column,row,"buddy1.png", dialog);
        return priest;
	}

	@Override
	public void render(float delta) {
		update();
		
		frameratecontrol();
		// set viewport
        Gdx.gl.glViewport((int) viewport.x, (int) viewport.y,(int) viewport.width, (int) viewport.height);
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		// draw
		batch.begin();
	    
	    // generic desktop interface
	    drawinterface();
	     	
	 	// draw equipment
	 	drawequipment();
        
        // overenemy description
        drawdescriptions();     

        //layercontrol();
        activemap=maplayers[game.getlayer()];
        
     	// dwaw background
        drawbackground();

        // draw static blocked tiles 
        drawtiles();
        
        // draw buddies
        drawbuddies();
        
        // draw consumables
        drawconsumables();
        
        // draw objects
        drawobjects();
        
        drawBullets();
        
        // draw enemies
        drawenemies();
        
        // draw hero
        drawhero(); 
        
        // draw interaction result
        if (just_interact==1) {
        	drawInteractionText();	
        }
        

        // draw debug mode info
        if (debug_mode==1) {
        	drawdebug();
        }
        
		// draw version build
        genericfont.draw(batch, "Development build "+WrapperEngine.BUILD_NUMBER, 10, (WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-620);

        
        // draw object inventory
        drawinventory();
        
        //draw android UI
        for(IButton button : buttons) {
			button.drawOnScreen(batch);
		}
        
        if (WrapperEngine.OUTPUT_OS.equals("android")) {
	    	drawandroidinterface();
	    	//debug_mode=1; // only for UI testing
	    }
        
        batch.end();
	}
	
	protected void setupsoundeffects() {
		die = Gdx.audio.newSound(Gdx.files.internal("soundeffects/die.ogg"));
		fight = Gdx.audio.newSound(Gdx.files.internal("soundeffects/fight.wav"));
		pickup = Gdx.audio.newSound(Gdx.files.internal("soundeffects/pickup.wav"));
		drink = Gdx.audio.newSound(Gdx.files.internal("soundeffects/drink.wav"));
		fireball = Gdx.audio.newSound(Gdx.files.internal("soundeffects/fireball.wav"));
	}

	protected void drawInteractionText() {
		screentext.drawScreen(batch, messagefont,interactionoutput,0.5f,40,Color.YELLOW);
	}
	
	protected void drawandroidinterface() {

	}

	/**
	 * 
	 */
	protected void drawinterface() {
		// draw character menu background
	 	batch.draw(layout.getmenubackground(),WrapperEngine.TILE_X_SIZE*WrapperEngine.ON_SCREEN_TILES_X,0);
	 	
	 	// draw action menu background
	 	//batch.draw(layout.getactionmenu(),0,WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y);
	 	 	
	 	// draw hero information
	 	//genericfont.draw(batch,"Hi "+prota.getname()+"!", (GameEngine.TILE_X_SIZE*GameEngine.ON_SCREEN_TILES_X)+25, (GameEngine.TILE_Y_SIZE*GameEngine.ON_SCREEN_TILES_Y)-30);
	 	genericfont.draw(batch,"Experience: "+prota.getexperience(), (WrapperEngine.TILE_X_SIZE*WrapperEngine.ON_SCREEN_TILES_X)+25, (WrapperEngine.WINDOWHEIGHT)-20);
	 	genericfont.draw(batch,"Life Points: "+prota.gethp(), (WrapperEngine.TILE_X_SIZE*WrapperEngine.ON_SCREEN_TILES_X)+25, (WrapperEngine.WINDOWHEIGHT)-40);
	 	genericfont.draw(batch,"Magic Points: "+prota.getmagic(), (WrapperEngine.TILE_X_SIZE*WrapperEngine.ON_SCREEN_TILES_X)+25, (WrapperEngine.WINDOWHEIGHT)-60);
	 	genericfont.draw(batch,"Resistance: "+prota.getresist(), (WrapperEngine.TILE_X_SIZE*WrapperEngine.ON_SCREEN_TILES_X)+25,(WrapperEngine.WINDOWHEIGHT)-80);
	 	genericfont.draw(batch,"Agility: "+prota.getagility(), (WrapperEngine.TILE_X_SIZE*WrapperEngine.ON_SCREEN_TILES_X)+25,(WrapperEngine.WINDOWHEIGHT)-100);
	 	genericfont.draw(batch,"Force: "+prota.getforce(), (WrapperEngine.TILE_X_SIZE*WrapperEngine.ON_SCREEN_TILES_X)+25,(WrapperEngine.WINDOWHEIGHT)-120);
	}

	/**
	 * 
	 */
	protected void drawinventory() {
		for (int i=0;i<WrapperEngine.INVENTORY_SIZE;i++) {
        	if (getobjinv().get_object(i)!=null) {
        		//genericfont.draw(batch,"Obj slot "+i+":"+objinv.get_object(i).getname(), 1000, (GameEngine.TILE_Y_SIZE*GameEngine.ON_SCREEN_TILES_Y)-(360+(i*20)));
                batch.draw(getobjinv().get_object(i).getsprite(), 1216,640-(i*64));

        	} else {
        		//genericfont.draw(batch,"Obj slot "+i+": available", 1000, (GameEngine.TILE_Y_SIZE*GameEngine.ON_SCREEN_TILES_Y)-(360+(i*20)));

        	}
        }
        
        // draw consumable inventory
        
        for (int i=0;i<WrapperEngine.INVENTORY_SIZE;i++) {
        	if (consinv.get_consumable(i)!=null) {
        		//genericfont.draw(batch,"Cons slot "+i+":"+consinv.get_consumable(i).getname(), 1000, (GameEngine.TILE_Y_SIZE*GameEngine.ON_SCREEN_TILES_Y)-(70+(i*20)));
        		batch.draw(consinv.get_consumable(i).getsprite(), 1152,640-(i*64));
        	} else {
        		//genericfont.draw(batch,"Cons slot "+i+": available", 1000, (GameEngine.TILE_Y_SIZE*GameEngine.ON_SCREEN_TILES_Y)-(70+(i*20)));

        	}
        }
	}
	
	protected void drawbackground() {
        for (int xpos=0;xpos<WrapperEngine.ON_SCREEN_TILES_X;xpos++) {
        	for (int ypos=0;ypos<WrapperEngine.ON_SCREEN_TILES_Y;ypos++) {
        				if (activemap.isdungeon()) {
        					batch.draw(activemap.getfreedungeontile(),xpos*WrapperEngine.TILE_X_SIZE,ypos*WrapperEngine.TILE_Y_SIZE);
        				} else {
        					batch.draw(activemap.getfreetile(),xpos*WrapperEngine.TILE_X_SIZE,ypos*WrapperEngine.TILE_Y_SIZE);
        				}
        	}
        }
		
	}

	/**
	 * 
	 */
	protected void drawdebug() {
		genericfont.draw(batch, "Screen Mouse X:"+Gdx.input.getX()+" Projected Mouse X: "+realXcoord, 20, (WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-20);
		genericfont.draw(batch, "Screen Mouse Y:"+Gdx.input.getY()+" Projected Mouse Y: "+realYcoord, 20, (WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-40);
		genericfont.draw(batch, "I'm at Screen X: "+ maplayers[game.getlayer()].getfirstxtile()+" Y: "+maplayers[game.getlayer()].getfirstytile(), 20, (WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-60);
		genericfont.draw(batch, "I'm at tile X: "+ (maplayers[game.getlayer()].getfirstxtile()+prota.getrelativextile(game.getactivemap()))+" Y: "+(maplayers[game.getlayer()].getfirstytile()+prota.getrelativeytile(game.getactivemap())), 20, (WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-160);

		genericfont.draw(batch, "Real screen size X:"+Gdx.graphics.getWidth(), 20, (WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-80);
		genericfont.draw(batch, "Real screen size Y:"+Gdx.graphics.getHeight(), 20, (WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-100);
		
		genericfont.draw(batch, "Eye mode:"+eye_mode, 20, (WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-120);
		genericfont.draw(batch, "Drop mode:"+object_drop_mode, 20, (WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-140);
		genericfont.draw(batch, "Layer:"+game.getlayer(), 20, (WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-180);
		genericfont.draw(batch, "Framerate:"+(int)(1/Gdx.graphics.getDeltaTime())+" FPS", 20, (WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-200);
		genericfont.draw(batch, "Enemies on screen:"+game.numberofenemiesonscreen(maplayers[game.getlayer()].getfirstxtile(),maplayers[game.getlayer()].getfirstytile()), 20, (WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-220);

		int i=0;
		for (AccessToLayer atl: maplayers[game.getlayer()].getLayerAccess()) {
			i++;
			genericfont.draw(batch, "Door on layer "+game.getlayer()+" to layer "+atl.getIncommingLayer()+" at "+atl.getIncommingX()+","+atl.getIncommingY(), 440, (WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-(20*i));			
		}
	}

	/**
	 * 
	 */
	protected void drawconsumables() {
		ListIterator<Consumable> consumableiterator = availableconsumables.listIterator();
        while (consumableiterator.hasNext()) {
        	//System.out.println("entra");
        	Consumable consumable=consumableiterator.next();
        	//System.out.println(bguy.getabsolutex());
        	if (consumable.consumableonscreen(maplayers[game.getlayer()].getfirstxtile(), maplayers[game.getlayer()].getfirstytile())==true) {
        		// draw consumable image		
    			if (consumable.getlayer()==game.getlayer()) {
    				batch.draw(consumable.getsprite(),(consumable.getabsolutex()-maplayers[game.getlayer()].getfirstxtile())*WrapperEngine.TILE_X_SIZE,(consumable.getabsolutey()-maplayers[game.getlayer()].getfirstytile())*WrapperEngine.TILE_Y_SIZE);       		
    			}
    		}
        }
	}

	/**
	 * 
	 */
	protected void drawobjects() {
		ListIterator<Object> objiterator = availableobjects.listIterator();
        while (objiterator.hasNext()) {
        	//System.out.println("entra");
        	Object obj=objiterator.next();
        	//System.out.println(bguy.getabsolutex());
        	if (obj.objectonscreen(maplayers[game.getlayer()].getfirstxtile(), maplayers[game.getlayer()].getfirstytile())==true) {
        		// draw object image
        		if (obj.getlayer()==game.getlayer()) { // if it is the correct layer
        			batch.draw(obj.getsprite(),(obj.getabsolutex()-maplayers[game.getlayer()].getfirstxtile())*WrapperEngine.TILE_X_SIZE,(obj.getabsolutey()-maplayers[game.getlayer()].getfirstytile())*WrapperEngine.TILE_Y_SIZE);       		
        		}
        	}
        }
	}
	
	protected void drawBullets() {
		for(Bullet bullet : bullets) {
			bullet.render(batch, activemap.getlayer());
		}
	}

	/**
	 * 
	 */
	protected void drawenemies() {
		ListIterator<Enemy> bgiterator = badguys.listIterator();
		
		int column = maplayers[game.getlayer()].getfirstxtile();
		int row = maplayers[game.getlayer()].getfirstytile();
		int layer = game.getlayer();
		
        while (bgiterator.hasNext()) {
        	//System.out.println("entra");
        	Enemy bguy=bgiterator.next();
        	//System.out.println(bguy.getabsolutex());
        	if (bguy.enemyonscreen(column, row, layer)) { // draw enemy image if the layer & position is correct
        			Sprite enemysprite=bguy.getsprite();
        			enemysprite.setPosition(getrelativeenemyxtileposition(bguy), getrelativeenemyytileposition(bguy));
        			//batch.draw(bguy.getsprite(),getrelativextileposition(bguy),getrelativeytileposition(bguy));
        			enemysprite.draw(batch);
        			Sprite energybar=layout.getenergybar();
        			Sprite redbar=layout.getredbar();
        			energybar.setPosition(getrelativeenemyxtileposition(bguy)+2, getrelativeenemyytileposition(bguy)+2);
        			energybar.draw(batch);
        			for (int i=0;i<(int)(bguy.percentlife()/10);i++) {
        				redbar.setPosition(getrelativeenemyxtileposition(bguy)+2+(i*6), getrelativeenemyytileposition(bguy)+2);
        				redbar.draw(batch);
        			}
        	}
        }
	}
	
	protected void drawhero() {
		int relX = prota.getrelativextile(game.getactivemap());
		int relY = prota.getrelativeytile(game.getactivemap());
		
		batch.draw(prota.getsprite(), relX*WrapperEngine.TILE_X_SIZE, relY*WrapperEngine.TILE_Y_SIZE);
		Sprite energybar=layout.getenergybar();
		Sprite redbar=layout.getredbar();
		energybar.setPosition(relX*WrapperEngine.TILE_X_SIZE+2, relY*WrapperEngine.TILE_Y_SIZE+2);
		energybar.draw(batch);
		for (int i=0;i<(int)(prota.percentlife()/10);i++) {
			redbar.setPosition(relX*WrapperEngine.TILE_X_SIZE+2+(i*6), relY*WrapperEngine.TILE_Y_SIZE+2);
			redbar.draw(batch);
		}
		
		Sprite magicbar=layout.getmagicbar();
		Sprite bluebar=layout.getbluebar();
		magicbar.setPosition(relX*WrapperEngine.TILE_X_SIZE+2, relY*WrapperEngine.TILE_Y_SIZE-4);
		magicbar.draw(batch);
		for (int i=0;i<(int)(prota.percentmagic()/10);i++) {
			bluebar.setPosition(relX*WrapperEngine.TILE_X_SIZE+2+(i*6), relY*WrapperEngine.TILE_Y_SIZE-4);
			bluebar.draw(batch);
		}
	}
	
	protected void drawbuddies() {
		ListIterator<Buddy> bgiterator = goodguys.listIterator();
        while (bgiterator.hasNext()) {
        	//System.out.println("entra");
        	Buddy bguy=bgiterator.next();
        	//System.out.println(bguy.getabsolutex());
        	if (bguy.buddyonscreen(maplayers[game.getlayer()].getfirstxtile(), maplayers[game.getlayer()].getfirstytile())==true) {
        		// draw buddie image if the layer is correct
        		if (bguy.getlayer()==game.getlayer()) {
        			Sprite buddysprite=bguy.getsprite();
        			buddysprite.setPosition(getrelativebuddyxtileposition(bguy), getrelativebuddyytileposition(bguy));
        			//batch.draw(bguy.getsprite(),getrelativextileposition(bguy),getrelativeytileposition(bguy));
        			buddysprite.draw(batch);
        		}
        	}
        }
	}

	/**
	 * @param bguy
	 * @return
	 */
	public int getrelativeenemyytileposition(Enemy bguy) {
		return (bguy.getabsolutey()-maplayers[game.getlayer()].getfirstytile())*WrapperEngine.TILE_Y_SIZE;
	}
	
	public int getrelativebuddyytileposition(Buddy gguy) {
		return (gguy.getabsolutey()-maplayers[game.getlayer()].getfirstytile())*WrapperEngine.TILE_Y_SIZE;
	}

	/**
	 * @param bguy
	 * @return
	 */
	public int getrelativeenemyxtileposition(Enemy bguy) {
		return (bguy.getabsolutex()-maplayers[game.getlayer()].getfirstxtile())*WrapperEngine.TILE_X_SIZE;
	}
	
	public int getrelativebuddyxtileposition(Buddy gguy) {
		return (gguy.getabsolutex()-maplayers[game.getlayer()].getfirstxtile())*WrapperEngine.TILE_X_SIZE;
	}
	
	public int getabsolutextile(Hero hero) {
		return hero.getabsolutecolumn(game.getactivemap());
	}
	
	public int getabsoluteytile(Hero hero) {
		return hero.getabsoluterow(game.getactivemap());
	}
	
	// layer control
	public void layerAccessCheck() {
		for (AccessToLayer atl: maplayers[game.getlayer()].getLayerAccess()) {
			if (prota.getabsolutecolumn(game.getactivemap())==atl.getIncommingX() && prota.getabsoluterow(game.getactivemap())==atl.getIncommingY()) {
	     		game.changelayer(atl,prota.getrelativextile(game.getactivemap()),prota.getrelativeytile(game.getactivemap())); //changelayer
	     		
	     		// update hero position next to the access
	     		prota.setrelativextile(game.getactivemap(), (atl.getOutcommingX()) % WrapperEngine.ON_SCREEN_TILES_X);
	     		prota.setrelativeytile(game.getactivemap(), (atl.getOutcommingY()) % WrapperEngine.ON_SCREEN_TILES_Y);

			}
		}		
		
		centerMapOn(prota);
	}
	
	private void centerMapOn(TileOccupier to) {
		int minX = 0, maxX = WrapperEngine.TOTAL_X_TILES - WrapperEngine.ON_SCREEN_TILES_X;
		int minY = 0, maxY = WrapperEngine.TOTAL_Y_TILES - WrapperEngine.ON_SCREEN_TILES_Y;
		
		int targetX = to.getabsolutecolumn(game.getactivemap()) - (WrapperEngine.ON_SCREEN_TILES_X / 2);
		int targetY = to.getabsoluterow(game.getactivemap()) - (WrapperEngine.ON_SCREEN_TILES_Y / 2);
		
		game.getactivemap().setfirstxtile(Math.max(minX, Math.min(maxX, targetX)));
		game.getactivemap().setfirstytile(Math.max(minY, Math.min(maxY, targetY)));
	}

	protected void drawtiles() {
		int firstX = game.getactivemap().getfirstxtile();
		int firstY = game.getactivemap().getfirstytile();
		
		int lastX = firstX + WrapperEngine.ON_SCREEN_TILES_X;
		int lastY = firstY + WrapperEngine.ON_SCREEN_TILES_Y;
		
		Tile tile = null;
        for (int xpos=firstX, relativex=0;xpos<lastX;xpos++, relativex++) {
        	for (int ypos=firstY, relativey=0;ypos<lastY;ypos++, relativey++) {
        		tile=activemap.getTile(xpos, ypos);
        			
        		if (tile.getshowimage()) {
        			if(tile.gettileimage() != null) {
        				batch.draw(tile.gettileimage(),relativex*WrapperEngine.TILE_X_SIZE,relativey*WrapperEngine.TILE_Y_SIZE);
        			}
        			if(tile.gettiledecoration() != null) {
        				batch.draw(tile.gettiledecoration(),relativex*WrapperEngine.TILE_X_SIZE,relativey*WrapperEngine.TILE_Y_SIZE);
        			}
        		}
        	}
        }
	}

	protected void drawdescriptions() {
		if (actualenemy!=null) {
        	if (actualenemy.getname()!=null) {
        		genericfont.draw(batch,"Enemy: "+actualenemy.getname(), (WrapperEngine.TILE_X_SIZE*WrapperEngine.ON_SCREEN_TILES_X)+25, (WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-400);
        		genericfont.draw(batch,"Life Points: "+actualenemy.gethp(), (WrapperEngine.TILE_X_SIZE*WrapperEngine.ON_SCREEN_TILES_X)+25,(WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-420);
        		genericfont.draw(batch,"Resistance: "+actualenemy.getresist(), (WrapperEngine.TILE_X_SIZE*WrapperEngine.ON_SCREEN_TILES_X)+25,(WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-440);
        		genericfont.draw(batch,"Agility: "+actualenemy.getagility(), (WrapperEngine.TILE_X_SIZE*WrapperEngine.ON_SCREEN_TILES_X)+25,(WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-460);
        		genericfont.draw(batch,"Force: "+actualenemy.getforce(), (WrapperEngine.TILE_X_SIZE*WrapperEngine.ON_SCREEN_TILES_X)+25,(WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-480);
        	}
        }
        
        // overconsumable description
        
        if (actualconsumable!=null) {
        	if (actualconsumable.getname()!=null) {
        		genericfont.draw(batch,"Consumable: "+actualconsumable.getname(), (WrapperEngine.TILE_X_SIZE*WrapperEngine.ON_SCREEN_TILES_X)+25, (WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-400);
        		genericfont.draw(batch,"+ Life Points: "+actualconsumable.getpoweruplife(), (WrapperEngine.TILE_X_SIZE*WrapperEngine.ON_SCREEN_TILES_X)+25,(WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-420);
        		genericfont.draw(batch,"+ Agility Points: "+actualconsumable.getpowerupagility(), (WrapperEngine.TILE_X_SIZE*WrapperEngine.ON_SCREEN_TILES_X)+25,(WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-440);
        	}
        }
        
        // overobject description
        
        if (actualobject!=null) {
        	if (actualobject.getname()!=null) {
        		genericfont.draw(batch,"Object: "+actualobject.getname(), (WrapperEngine.TILE_X_SIZE*WrapperEngine.ON_SCREEN_TILES_X)+25, (WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-400);
        		genericfont.draw(batch,"+ defense: "+actualobject.getdefense(), (WrapperEngine.TILE_X_SIZE*WrapperEngine.ON_SCREEN_TILES_X)+25,(WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-420);
        		genericfont.draw(batch,"+ offense: "+actualobject.getattack(), (WrapperEngine.TILE_X_SIZE*WrapperEngine.ON_SCREEN_TILES_X)+25,(WrapperEngine.TILE_Y_SIZE*WrapperEngine.ON_SCREEN_TILES_Y)-440);
        	}
        }
	}

	/**
	 * 
	 */
	protected void drawequipment() {
		if (prota.gethead().getname()!=null) {
	 		batch.draw(prota.gethead().getsprite(),970,545);
	 		//genericfont.draw(batch,prota.gethead().getname(), 930,619);
	 		genericfont.draw(batch,"A:+"+prota.gethead().getattack()+" D:+"+prota.gethead().getdefense()+" Dur:"+prota.gethead().getdurability(), 927,535);
        } else {
        	//genericfont.draw(batch,"Head: nothing", (GameEngine.TILE_X_SIZE*GameEngine.ON_SCREEN_TILES_X)+25,(GameEngine.TILE_Y_SIZE*GameEngine.ON_SCREEN_TILES_Y)-170);

        }
        if (prota.getlefthand().getname()!=null) {
        	batch.draw(prota.getlefthand().getsprite(),1050,448);
        	//genericfont.draw(batch,prota.getlefthand().getname(), 1018,532 );
        	genericfont.draw(batch,"A:+"+prota.getlefthand().getattack()+" D:+"+prota.getlefthand().getdefense()+" Dur:"+prota.getlefthand().getdurability(), 1010,428 );
        } else {
        	//genericfont.draw(batch,"Left hand: nothing", (GameEngine.TILE_X_SIZE*GameEngine.ON_SCREEN_TILES_X)+25,(GameEngine.TILE_Y_SIZE*GameEngine.ON_SCREEN_TILES_Y)-190);

        }
        if (prota.getrighthand().getname()!=null) {
        	batch.draw(prota.getrighthand().getsprite(),872,448);
        	//genericfont.draw(batch,prota.getrighthand().getname(), 842,532);
        	genericfont.draw(batch,"A:+"+prota.getrighthand().getattack()+" D:+"+prota.getrighthand().getdefense()+" Dur:"+prota.getrighthand().getdurability(), 849,428);
        } else {
        	//genericfont.draw(batch,"Right hand: nothing", (GameEngine.TILE_X_SIZE*GameEngine.ON_SCREEN_TILES_X)+25,(GameEngine.TILE_Y_SIZE*GameEngine.ON_SCREEN_TILES_Y)-210);

        }
        if (prota.getbody().getname()!=null) {
        	batch.draw(prota.getbody().getsprite(),971,448);
        	//genericfont.draw(batch,prota.getbody().getname(),931,532);
        	genericfont.draw(batch,"A:+"+prota.getbody().getattack()+" D:+"+prota.getbody().getdefense()+" Dur:"+prota.getbody().getdurability(),931,442);
        } else {
        	//genericfont.draw(batch,"Body: nothing", (GameEngine.TILE_X_SIZE*GameEngine.ON_SCREEN_TILES_X)+25,(GameEngine.TILE_Y_SIZE*GameEngine.ON_SCREEN_TILES_Y)-230);

        }
        if (prota.getfoot().getname()!=null) {
        	batch.draw(prota.getfoot().getsprite(),971,350);
        	//genericfont.draw(batch,prota.getfoot().getname(),931,425);
        	genericfont.draw(batch,"A:+"+prota.getfoot().getattack()+" D:+"+prota.getfoot().getdefense()+" Dur:"+prota.getfoot().getdurability(),931,330);
        } else {
        	//genericfont.draw(batch,"Foot: nothing", (GameEngine.TILE_X_SIZE*GameEngine.ON_SCREEN_TILES_X)+25,(GameEngine.TILE_Y_SIZE*GameEngine.ON_SCREEN_TILES_Y)-250);
        }
	}
	void frameratecontrol() {
		//  delay for each frame  -   time it took for one frame 
		long time = (1000 / WrapperEngine.FPS); 
        
        if (time > 0) 
        { 
                try 
                { 
                        Thread.sleep(time); 
                } 
                catch(Exception e){} 
        } 
	}
	
	void update()
    { 
		updateButtons();
		updateBullets();
		updateLockables();
		
		// random elements generator
    	Random randomGenerator = new Random();
    	int number=randomGenerator.nextInt(6); // 50% chances to create something
    	if (number==0) { // create enemy
    		game.createrandomenemy();
    	}
    	if (number==1) { // create consumable
    		game.createrandomconsumable(true,0,0,0,WrapperEngine.NUMBER_OF_CONSUMABLES_PER_LOOP);
    		
    	}
    	if (number==2) { // create object
    		game.createrandomobject(true,0,0,0,WrapperEngine.NUMBER_OF_OBJECTS_PER_LOOP);
    	}
    	// get relative mouse coord instead of real ones
    	realXcoord=(int)((float)Gdx.input.getX()*(float)((float)WrapperEngine.WINDOWWIDTH/(float)Gdx.graphics.getWidth()));
		realYcoord=(int)((float)Gdx.input.getY()*(float)((float)WrapperEngine.WINDOWHEIGHT/(float)Gdx.graphics.getHeight()))*-1+(WrapperEngine.WINDOWHEIGHT);
    	//realXcoord=(int)((float)Gdx.input.getX()*(float)((float)WrapperEngine.WINDOWWIDTH/(float)viewport.x));
    	//realYcoord=(int)((float)Gdx.input.getY()*(float)((float)WrapperEngine.WINDOWHEIGHT/(float)viewport.y))*-1+(WrapperEngine.WINDOWHEIGHT);
    	if (WrapperEngine.OUTPUT_OS.equals("android")) { 
    		//realYcoord=realYcoord+WrapperEngine.ANDROID_MENU_BAR_SIZE;
    	}
    	// mouse events control
    	handlemouseinput(); 
    	
    	// end mouse events control
    	
    	// key events control
    	handlekeyboardinput();
        
        if(animateHeroInPlace) {
        	animateHeroDuration += 1;
        	
        	if(animateHeroDuration >= animateHeroDelay) {
        		prota.changeDirection(prota.getDirection());
        		animateHeroDuration = 0;
        	}
        }
        
        this.walkPath();
    }
	
	// Keyboard event handler
	
	@Override
	public boolean keyUp (int keycode) {
	    if(WrapperEngine.STOPSONFIRE && bullets.size() > 0) return false;
	    
		if (keycode==Keys.P) { // debug mode
			if (debug_mode==0) { debug_mode=1; } else { 
				debug_mode=0; 
			}
		}
		if (keycode==Keys.L) {
			look();
		}
		if (keycode==Keys.H) {
			fight();
		}
		if (keycode==Keys.E) {
			magic();
		}
		if (keycode==Keys.T) {
			talk();
		}
		if (keycode==Keys.G) {
			take();
		}
		if (keycode==Keys.D) {
			drop();
		}
		if (keycode==Keys.Z) {
			dispose();
		}
		if (keycode==Keys.O) {
			// ENABLE OBJECT INVENTORY MODE
        	object_inv_mode=1;
    		consumable_inv_mode=0;
    		object_drop_mode=0;
    		just_interact=0;
		}
		if (keycode==Keys.C) {
			// ENABLE CONSUMABLE INVENTORY MODE
        	object_inv_mode=0;
    		consumable_inv_mode=1;
    		object_drop_mode=0;
    		just_interact=0;
		}
		
		// OBJECT INVENTORY ACTIONS
        if (keycode==Keys.NUM_1 && object_inv_mode==1) {
        	getobject(getobjinv().get_object(1),1);
        }
        if (keycode==Keys.NUM_2 && object_inv_mode==1) {
        	getobject(getobjinv().get_object(2),2);
        }
        if (keycode==Keys.NUM_3 && object_inv_mode==1) {
        	getobject(getobjinv().get_object(3),3);
        }
        if (keycode==Keys.NUM_4 && object_inv_mode==1) {
        	getobject(getobjinv().get_object(4),4);
        }
        if (keycode==Keys.NUM_5 && object_inv_mode==1) {
        	getobject(getobjinv().get_object(5),5);
        }
        if (keycode==Keys.NUM_6 && object_inv_mode==1) {
        	getobject(getobjinv().get_object(6),6);
        }
        if (keycode==Keys.NUM_7 && object_inv_mode==1) {
        	getobject(getobjinv().get_object(7),7);
        }
        if (keycode==Keys.NUM_8 && object_inv_mode==1) {
        	getobject(getobjinv().get_object(8),8);
        }
        if (keycode==Keys.NUM_9 && object_inv_mode==1) {
        	getobject(getobjinv().get_object(9),9);
        }
        if (keycode==Keys.NUM_0 && object_inv_mode==1) {
        	getobject(getobjinv().get_object(0),0);
        }
        
        // OBJECT DROP INVENTORY ACTIONS
        if (keycode==Keys.NUM_1 && object_drop_mode==1) {
        	getobjinv().delete_object(1);
        }
        if (keycode==Keys.NUM_2 && object_drop_mode==1) {
        	getobjinv().delete_object(2);
        }
        if (keycode==Keys.NUM_3 && object_drop_mode==1) {
        	getobjinv().delete_object(3);
        }
        if (keycode==Keys.NUM_4 && object_drop_mode==1) {
        	getobjinv().delete_object(4);
        }
        if (keycode==Keys.NUM_5 && object_drop_mode==1) {
        	getobjinv().delete_object(5);
        }
        if (keycode==Keys.NUM_6 && object_drop_mode==1) {
        	getobjinv().delete_object(6);
        }
        if (keycode==Keys.NUM_7 && object_drop_mode==1) {
        	getobjinv().delete_object(7);
        }
        if (keycode==Keys.NUM_8 && object_drop_mode==1) {
        	getobjinv().delete_object(8);
        }
        if (keycode==Keys.NUM_9 && object_drop_mode==1) {
        	getobjinv().delete_object(9);
        }
        if (keycode==Keys.NUM_0 && object_drop_mode==1) {
        	getobjinv().delete_object(0);
        }
        // CONSUMABLE INVENTORY ACTIONS
        if (keycode==Keys.NUM_1 && consumable_inv_mode==1) {
        	getconsumable(consinv.get_consumable(1));
        	consinv.delete_consumable(1);
        }
        if (keycode==Keys.NUM_2 && consumable_inv_mode==1) {
        	getconsumable(consinv.get_consumable(2));
        	consinv.delete_consumable(2);
        }
        if (keycode==Keys.NUM_3 && consumable_inv_mode==1) {
        	getconsumable(consinv.get_consumable(3));
        	consinv.delete_consumable(3);
        }
        if (keycode==Keys.NUM_4 && consumable_inv_mode==1) {
        	getconsumable(consinv.get_consumable(4));
        	consinv.delete_consumable(4);
        }
        
        if (keycode==Keys.NUM_5 && consumable_inv_mode==1) {
        	getconsumable(consinv.get_consumable(5));
        	consinv.delete_consumable(5);
        }
        if (keycode==Keys.NUM_6 && consumable_inv_mode==1) {
        	getconsumable(consinv.get_consumable(6));
        	consinv.delete_consumable(6);
        }
        if (keycode==Keys.NUM_7 && consumable_inv_mode==1) {
        	getconsumable(consinv.get_consumable(7));
        	consinv.delete_consumable(7);
        }
        if (keycode==Keys.NUM_8 && consumable_inv_mode==1) {
        	getconsumable(consinv.get_consumable(8));
        	consinv.delete_consumable(8);
        }
        if (keycode==Keys.NUM_9 && consumable_inv_mode==1) {
        	getconsumable(consinv.get_consumable(9));
        	consinv.delete_consumable(9);
        }
        if (keycode==Keys.NUM_0 && consumable_inv_mode==1) {
        	getconsumable(consinv.get_consumable(0));
        	consinv.delete_consumable(0);
        }
		return false;
	}

	protected void handlekeyboardinput() {
	    if(WrapperEngine.STOPSONFIRE && bullets.size() > 0) return;
	    
		boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
		
		if (Gdx.input.isKeyPressed(Keys.DPAD_RIGHT)) { 
			if(shift) prota.changeDirection(Directions.EAST);
			else goright(); 
		} 
        
		if (Gdx.input.isKeyPressed(Keys.DPAD_LEFT)) { 
			if(shift) prota.changeDirection(Directions.WEST);
			else goleft(); 
		}
		
        if (Gdx.input.isKeyPressed(Keys.DPAD_UP)) { 
        	if(shift) prota.changeDirection(Directions.SOUTH);
			else goup(); 
        } 
        
        if (Gdx.input.isKeyPressed(Keys.DPAD_DOWN)) { 
        	if(shift) prota.changeDirection(Directions.NORTH);
			else godown();
        }
	}

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	protected void handlemouseinput() {
	    if(WrapperEngine.STOPSONFIRE && bullets.size() > 0) return;
	    
	    boolean captured = false;
		if (WrapperEngine.OUTPUT_OS=="desktop") { captured = handlemousedesktopinput(); }
		if (WrapperEngine.OUTPUT_OS=="android") { captured = handletouchandroidinput(); }
		
		if(captured) return;
		else captured = (!Gdx.input.isTouched() && this.touchedLastFrame) && screentext.onMouseClicked(); 
		
		if((!Gdx.input.isTouched() && this.touchedLastFrame) && !captured && just_interact == 0 && activemap != null) {
			Tile clicked = activemap.getTileAtPosition(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
			
			if(clicked == null) {
				this.touchedLastFrame = Gdx.input.isTouched();
				return;
			}
			
			if(activemap.isUnreachable(clicked)) {
				alert("You can not reach that area from here!");
				this.touchedLastFrame = Gdx.input.isTouched();
				return;
			}
			
			Tile start = prota.gettile(activemap);
			
			if(heroPathing == null || heroPathing.getMap() != activemap) {
				heroPathing = new Pathing<Hero>(activemap);
			}
			
			lastPath = heroPathing.getPath(start, clicked);
			lastPath.search();
			
			if(!lastPath.isCompletePath()) {
				lastPath = null;

				this.alert("You cannot get there from here!");
			}
		} else if(captured) {
			lastPath = null;
		}
		
		this.touchedLastFrame = Gdx.input.isTouched();
	}
	
	protected boolean handletouchandroidinput() {
	    if(WrapperEngine.STOPSONFIRE && bullets.size() > 0) return false;
	    
		if (Gdx.input.isTouched()) {
			for(IButton button : buttons) {
				if(button.getIsMouseover()) return true;
			}
			
    		// CONSUMABLE INVENTORY ACTIONS
    		for (int i=0;i<WrapperEngine.INVENTORY_SIZE;i++) {
    			if (realXcoord>1152 && realXcoord<1216 && realYcoord>640-(64*i) && realYcoord<704-(64*i) && eye_mode==0) {
    				getconsumable(consinv.get_consumable(i));
    				consinv.delete_consumable(i);
        			return true;
    			}
            }
    		// OBJECT INVENTORY ACTIONS
    		for (int i=0;i<WrapperEngine.INVENTORY_SIZE;i++) {
    			if (realXcoord>1216 && realXcoord<1280 && realYcoord>640-(64*i) && realYcoord<704-(64*i) && object_drop_mode==0 && eye_mode==0) {
    				getobject(getobjinv().get_object(i),i);
        			return true;
    			}
            }
    		// OBJECT INVENTORY DROP
    		for (int i=0;i<WrapperEngine.INVENTORY_SIZE;i++) {
    			if (realXcoord>1216 && realXcoord<1280 && realYcoord>640-(64*i) && realYcoord<704-(64*i) && object_drop_mode==1  && eye_mode==0) {
    				getobjinv().delete_object(i);
        			return true;
    			}
            }
    		// EYEMODE OBJECT INVENTORY
    		for (int i=0;i<WrapperEngine.INVENTORY_SIZE;i++) {
    			if (realXcoord>1216 && realXcoord<1280 && realYcoord>640-(64*i) && realYcoord<704-(64*i) && eye_mode==1) {
    				actualobject=getobjinv().get_object(i);
        			return true;
    			}
            }
    		// EYEMODE CONSUMABLE INVENTORY
    		for (int i=0;i<WrapperEngine.INVENTORY_SIZE;i++) {
    			if (realXcoord>1152 && realXcoord<1216 && realYcoord>640-(64*i) && realYcoord<704-(64*i) && eye_mode==1) {
    				actualconsumable=consinv.get_consumable(i);
        			return true;
    			}
            }	
			
		}
		
		return false;
	}
	
	
	protected boolean handlemousedesktopinput() {
	    if(WrapperEngine.STOPSONFIRE && bullets.size() > 0) return false;
	    
		if (Gdx.input.isTouched()) {
			for(IButton button : buttons) {
				if(button.getIsMouseover()) return true;
			}
			
    		// CONSUMABLE INVENTORY ACTIONS
    		for (int i=0;i<WrapperEngine.INVENTORY_SIZE;i++) {
    			if (realXcoord>1152 && realXcoord<1216 && realYcoord>640-(64*i) && realYcoord<704-(64*i) && eye_mode==0) {
    				getconsumable(consinv.get_consumable(i));
    				consinv.delete_consumable(i);
        			return true;
    			}
            }
    		// OBJECT INVENTORY ACTIONS
    		for (int i=0;i<WrapperEngine.INVENTORY_SIZE;i++) {
    			if (realXcoord>1216 && realXcoord<1280 && realYcoord>640-(64*i) && realYcoord<704-(64*i) && object_drop_mode==0 && eye_mode==0) {
    				getobject(getobjinv().get_object(i),i);
        			return true;
    			}
            }
    		// OBJECT INVENTORY DROP
    		for (int i=0;i<WrapperEngine.INVENTORY_SIZE;i++) {
    			if (realXcoord>1216 && realXcoord<1280 && realYcoord>640-(64*i) && realYcoord<704-(64*i) && object_drop_mode==1  && eye_mode==0) {
    				getobjinv().delete_object(i);
        			return true;
    			}
            }
    		// EYEMODE OBJECT INVENTORY
    		for (int i=0;i<WrapperEngine.INVENTORY_SIZE;i++) {
    			if (realXcoord>1216 && realXcoord<1280 && realYcoord>640-(64*i) && realYcoord<704-(64*i) && eye_mode==1) {
    				actualobject=getobjinv().get_object(i);
        			return true;
    			}
            }
    		// EYEMODE CONSUMABLE INVENTORY
    		for (int i=0;i<WrapperEngine.INVENTORY_SIZE;i++) {
    			if (realXcoord>1152 && realXcoord<1216 && realYcoord>640-(64*i) && realYcoord<704-(64*i) && eye_mode==1) {
    				actualconsumable=consinv.get_consumable(i);
        			return true;
    			}
            }	
    		
    	}
		
		return false;
	}
	
    void getobject(Object obj,int pos) {
    	if (obj!=null) {
			// if object exists
    		pickup.play();
			if (obj.getposition()=="head") {
				if (prota.gethead().getname()==null) {
					prota.sethead(obj);
					getobjinv().delete_object(pos);
				} else {
					getobjinv().set_object(pos,prota.gethead());
					prota.sethead(obj);
				}
			}
			if (obj.getposition()=="righthand") {
				if (prota.getrighthand().getname()==null) {
					prota.setrighthand(obj);
					getobjinv().delete_object(pos);
				} else {
					getobjinv().set_object(pos,prota.getrighthand());
					prota.setrighthand(obj);
				}
			}
			if (obj.getposition()=="lefthand") {
				if (prota.getlefthand().getname()==null) {
					prota.setlefthand(obj);
					getobjinv().delete_object(pos);
				} else {
					getobjinv().set_object(pos,prota.getlefthand());
					prota.setlefthand(obj);
				}	
			}
			if (obj.getposition()=="body") {
				if (prota.getbody().getname()==null) {
					prota.setbody(obj);
					getobjinv().delete_object(pos);
				} else {
					getobjinv().set_object(pos,prota.getbody());
					prota.setbody(obj);
				}
			}
			if (obj.getposition()=="foot") {
				if (prota.getfoot().getname()==null) {
					prota.setfoot(obj);
					getobjinv().delete_object(pos);
				} else {
					getobjinv().set_object(pos,prota.getfoot());
					prota.setfoot(obj);
				}
			}
    	}
    }
    
    void getconsumable(Consumable obj) {
    	if (obj!=null) {
			// if consumable exists
			prota.updateagility(obj.getpowerupagility());
			prota.updatehp(obj.getpoweruplife());
			drink.play();
    	}
    }
    
    public void fight() {
    	object_inv_mode=0;
		consumable_inv_mode=0;
		object_drop_mode=0;
    	//boolean resultoffight=false;
		String resultoffight;
    	//actualenemy=game.overenemy(); // get the enemy (if exist)
		actualenemy=game.nexttoenemy(); // get the enemy (if exist)
		if (actualenemy.getname()!=null) {
			fight.play();
			if (!BackgroundMusic.playingfight) {
	    		BackgroundMusic.stopall();
	    		BackgroundMusic.startfight();
	    		BackgroundMusic.playingfight=true;
	    	}
			//resultoffight=prota.fight(actualenemy);
			resultoffight=prota.hit(actualenemy);
			//System.out.println("FIGHT!");
			// if hero wins
			if (resultoffight=="ENEMYDEAD") {
				// if you win
				killEnemy(actualenemy);
			} 
			if (resultoffight=="HERODEAD") {
				BackgroundMusic.stopall();
	    		BackgroundMusic.startoutside();
	    		BackgroundMusic.playingfight=false;
				game.herodies();
				die.play();
				interactionoutput="You lose the battle,\nyou awake in a strange place!";
			}
			if (resultoffight!="ENEMYDEAD" && resultoffight!="HERODEAD") {
				interactionoutput=resultoffight;
			}
		just_interact=1;
		}
    }

	protected void killEnemy(Enemy enemy) {
		prota.updateexperience(100);
		//System.out.println("YOU WIN!");
		if (enemy.getname()=="megaboss") {
				interactionoutput="You get the amulet, you win the game!!";
		} else {
			interactionoutput="Great! You win the battle!!";
			BackgroundMusic.stopall();
			if (activemap.isdungeon()) {
				BackgroundMusic.startdungeon();
			} else {
				BackgroundMusic.startoutside();
			}
			BackgroundMusic.playingfight=false;
			// unblock enemy tile
			maplayers[game.getlayer()].unblocktile(enemy.getabsolutex(), enemy.getabsolutey());
			// enemies drop objects
			Random randomGenerator = new Random();
			int type = randomGenerator.nextInt(4); // 50% chances to drop object / consumable
			switch (type) {
			case 0:
				game.createrandomconsumable(false, game.getlayer(), enemy.getabsolutex(), enemy.getabsolutey(),1);
				break;
			case 1:
				game.createrandomobject(false, game.getlayer(), enemy.getabsolutex(), enemy.getabsolutey(),1);
				break;
			}  
			
			if(Math.random() < 100 / WrapperEngine.KEY_DROP_RATE) {
				game.createkeyobject(game.getlayer(), enemy.getabsolutex(), enemy.getabsolutey());
			}
		}
		game.removeenemy(enemy);
	}
	
    public boolean goup() {
    	object_inv_mode=0;
    	object_drop_mode=0;
		consumable_inv_mode=0;
		eye_mode=0;
		just_interact=0;
    	actualenemy=null;
    	actualconsumable=null;
    	actualobject=null;
    	if(game.heroup()) {
	    	layerAccessCheck(); // layer control
	    	// activate enemies
	    	game.activateenemies(maplayers[game.getlayer()].getfirstxtile(),maplayers[game.getlayer()].getfirstytile());
	    	// moving active enemies
	    	game.moveenemies();
	    	return true;
    	}
    	return false;
    }
    public boolean godown() {
    	object_inv_mode=0;
    	object_drop_mode=0;
		consumable_inv_mode=0;
		eye_mode=0;
		just_interact=0;
    	actualenemy=null;
    	actualconsumable=null;
    	actualobject=null;
    	if(game.herodown()) {
	    	layerAccessCheck(); // layer control
	    	// activate enemies
	    	game.activateenemies(maplayers[game.getlayer()].getfirstxtile(),maplayers[game.getlayer()].getfirstytile());
	    	// moving active enemies
	    	game.moveenemies();
	    	return true;
    	}
    	return false;
    }
    
    public boolean goleft() {
    	object_inv_mode=0;
    	object_drop_mode=0;
		consumable_inv_mode=0;
		eye_mode=0;
		just_interact=0;
    	actualenemy=null;
    	actualconsumable=null;
    	actualobject=null;
    	if(game.heroleft()) {
	    	layerAccessCheck(); // layer control
	    	// activate enemies
	    	game.activateenemies(maplayers[game.getlayer()].getfirstxtile(),maplayers[game.getlayer()].getfirstytile());
	    	// moving active enemies
	    	game.moveenemies();
	    	return true;
    	}
    	return false;
    }
    public boolean goright() {
    	eye_mode=0;
    	just_interact=0;
		object_inv_mode=0;
		object_drop_mode=0;
		consumable_inv_mode=0;
		actualenemy=null;
		actualconsumable=null;
		actualobject=null;
		if(game.heroright()) {
	    	layerAccessCheck(); // layer control
			// activate enemies
	    	game.activateenemies(maplayers[game.getlayer()].getfirstxtile(),maplayers[game.getlayer()].getfirstytile());
	    	// moving active enemies
	    	game.moveenemies();
	    	return true;
    	}
    	return false;
    }
    public void look() {
    	eye_mode=1;
    	object_inv_mode=0;
    	object_drop_mode=0;
		consumable_inv_mode=0;
		just_interact=0;
    	actualenemy=game.overenemy(); // get the enemy (if exist)
    	actualconsumable=game.overconsumable(); // get the consumable (if exist)
    	actualobject=game.overobject(); // get the object (if exist)
    }
    public void talk() {
    	eye_mode=0;
    	object_inv_mode=0;
		consumable_inv_mode=0;
		object_drop_mode=0;
		//actualenemy=game.overenemy(); // get the enemy (if exist)
		actualenemy=game.nexttoenemy(); // get the enemy (if exist)
		//actualbuddy=game.overbuddy(); // get the buddy (if exist)
		actualbuddy=game.nexttobuddy(); // get the enemy (if exist)
		if (actualenemy.getname()!=null) {
			interactionoutput=actualenemy.talk();
			just_interact=1;
		}
		if (actualbuddy.getname()!=null) {
			interactionoutput=actualbuddy.talk();
			just_interact=1;
		}
    }
    public void alert(String message) {
    	interactionoutput=message;
		just_interact=1;
    }
    public void take() {
    	eye_mode=0;
    	object_inv_mode=0;
		consumable_inv_mode=0;
		object_drop_mode=0;
		just_interact=0;
    	// get consumable into inventory
		actualconsumable=game.overconsumable(); // get the consumable (if exist)
		
		Map map = game.getactivemap();
		givetohero(actualconsumable, map.getlayer(), prota.getabsolutecolumn(map), prota.getabsoluterow(map));
		
		// get object into inventory
		actualobject=game.overobject(); // get the consumable (if exist)
		givetohero(actualobject, map.getlayer(), prota.getabsolutecolumn(map), prota.getabsoluterow(map));
    }

    public void givetohero(Object obj, int layer, int column, int row) {
    	if (obj.getname()!=null) {
			if (getobjinv().getfreeslot()!=-1) {
				getobjinv().set_object(getobjinv().getfreeslot(), obj);
				game.removeobject(obj);
				pickup.play();
			} else if(!game.hasobject(obj)) {
				game.addobject(obj, layer, column, row);
			}
		}	
    }
    
	public void givetohero(Consumable cons, int layer, int column, int row) {
		if (cons.getname()!=null) {
			// if consumable exists
			if (consinv.getfreeslot()!=-1) {
				consinv.set_consumable(consinv.getfreeslot(), cons);
				pickup.play();
				game.removeconsumable(actualconsumable);
			} else if(!game.hasconsumable(cons)){
				game.addconsumable(cons, layer, column, row);
			}
		}
	}
    
    public void drop() {
		// ENABLE CONSUMABLE INVENTORY MODE
    	eye_mode=0;
    	object_inv_mode=0;
		consumable_inv_mode=0;
		object_drop_mode=1;
		just_interact=0;
	}
    // Original class methods
	@Override
	public void resize(int width, int height) {
		// calculate new viewport
		
        float aspectRatio = (float)width/(float)height;
        float scale = 1f;
        Vector2 crop = new Vector2(0f, 0f);
        if(aspectRatio > WrapperEngine.ASPECT_RATIO)
        {
            scale = (float)height/(float)WrapperEngine.VIRTUAL_HEIGHT;
            crop.x = (width - WrapperEngine.VIRTUAL_WIDTH*scale)/2f;
        }
        else if(aspectRatio < WrapperEngine.ASPECT_RATIO)
        {
            scale = (float)width/(float)WrapperEngine.VIRTUAL_WIDTH;
            crop.y = (height - WrapperEngine.VIRTUAL_HEIGHT*scale)/2f;
        }
        else
        {
            scale = (float)width/(float)WrapperEngine.VIRTUAL_WIDTH;
        }

        float w = (float)WrapperEngine.VIRTUAL_WIDTH*scale;
        float h = (float)WrapperEngine.VIRTUAL_HEIGHT*scale;
        viewport = new Rectangle(crop.x, crop.y, w, h); 
        
        for(IButton button : buttons) {
        	button.handleResize();
        }
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
	
	@Override
	public void dispose() {
		if(batch != null) batch.dispose();
		Gdx.app.exit();
	}

	public Map getmaplayer(int value) {
		Map result = maplayers[value];
		if(result == null) { // inside init
			result = game.getmaplayer(value);	// try wrapper's array
		}
		
		return result;
	}
	
	@Override 
	public boolean touchUp(int x, int y, int pointer, int button) {
		//return screentext.onMouseClicked();  // should work on touchUp and mouseUp
		return false;
	}
	
	public void updateBullets() {
		if(bullets.size() > 0) {
			LinkedList<Bullet> toRemove = new LinkedList<Bullet>();
			
			for(Bullet bullet : bullets) {
				bullet.update();
				
				if(bullet.isFinished()) {
					toRemove.push(bullet);
				}
			}
			
			for(Bullet bullet : toRemove) {
				bullets.remove(bullet);
			}
		}
	}
	
	public void magic() {
		Bullet bullet = prota.fireBullet();
		if(bullet != null) { 
			bullets.push(bullet); 
			fireball.play(); 
		}
	}
	
	public void enemyTurn() {
		// activate enemies
    	game.activateenemies(maplayers[game.getlayer()].getfirstxtile(),maplayers[game.getlayer()].getfirstytile());
    	// moving active enemies
    	game.moveenemies();
	}

	public PopupInfoText getScreentext() {
		return screentext;
	}

	public void setScreentext(PopupInfoText screentext) {
		this.screentext = screentext;
	}

	public Buddy createchest(Map map, int i, int j) {
		Buddy chest = game.createchest(map.getlayer(), i, j);
        return chest;
	}
	
	public Buddy createdoor(Map map, int i, int j) {
		Buddy door = game.createDoor(map.getlayer(), i, j);
		return door;
	}

	public Object_inventory getobjinv() {
		return objinv;
	}
	
	public void updateLockables() {
		LinkedList<Buddy> toRemove = new LinkedList<Buddy>();
		
		for(Buddy buddy : this.goodguys) {
			buddy.update();
			
			if(buddy instanceof Chest) {
				if(((Chest)buddy).getShouldKill()) {
					toRemove.push(buddy);
				}
			}
			
			if(buddy instanceof Door) {
				if(((Door)buddy).getShouldKill()) {
					toRemove.push(buddy);
				}
			}
		}
		
		for(Buddy buddy : toRemove) {
			killBuddy(buddy);
		}
	}
	
	public WrapperEngine getgame() { return game; }

	public void killBuddy(Buddy buddy) {
		maplayers[game.getlayer()].unblocktile(buddy.getabsolutex(), buddy.getabsolutey());
		game.removebuddy(buddy);
	}
	
	public void walkPath() {
		if(lastPath != null) {
			heroPathing.walk(lastPath, prota);
			
			if(prota.gettile(game.getactivemap()) == lastPath.getEnd()) {
				stopPathing();
			}
		}
	}
	
	public void stopPathing() {
		lastPath = null;
	}
	
	public void updateButtons() {
		for(IButton button : buttons) {
			button.update();
		}
	}
	
	public IButton getButton(String text) {
		for(IButton button : buttons) {
			if(button instanceof BaseButton) {
				if(((BaseButton)button).getText().equals(text)) {
					return button;
				}
			}
		}
		
		return null;
	}
	
	public void closeScreenText() {
		just_interact = 0;
		this.screentext.handleClosed();
	}

	public void exit() {
		dispose();
	}

	public void createDirectAccessPoint(int myLayer, int column, int row) {
		this.getmaplayer(myLayer).createAccessDungeon(column, row, column, row, myLayer + 1);
		this.getmaplayer(myLayer + 1).createAccessDungeon(column, row, column, row, myLayer);
	}
}
