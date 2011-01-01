package game;

import game.DataBattle.TileOverlay;

import java.util.*;
import util.*;
import java.awt.Color;
import java.awt.Image;

import javax.xml.soap.MimeHeader;

public class AgentInfo {
	//an ability
	public static abstract class Ability {
		public abstract void apply(Agent src, Agent target, Vec trg);
		public abstract void select(Agent src);
		public abstract int range();
		public abstract int damage();
		public final String getName() {
			return mName;
		}
		public final String getDesc() {
			return mDesc;
		}
		public final void setName(String s) {
			mName = s;
		}
		public final void setDesc(String s) {
			mDesc = s;
		}
		private String mDesc = "";
		private String mName = "";
	}
	
	public static class AbilityDamageGeneric extends Ability {
		public AbilityDamageGeneric(String name, String desc, int range, int dmg) {
			setName(name);
			setDesc(desc);
			mRange = range;
			mDamage = dmg;
		}
		public void select(Agent src) {
			src.getBoard().attackFlood(src.getPos(), mRange, TileOverlay.Neg);
		}
		public void apply(Agent src, Agent target, Vec trg) {
			if (target != null)
				target.damage(mDamage);
		}
		public int range() {
			return mRange;
		}
		public int damage() {
			return mDamage;
		}
		private int mDamage;
		private int mRange;
	}
	
	protected void setAbilities(Ability[] abilities) {
		mAbilities = abilities;
	}
	
	public Ability[] getAbilities() {
		return mAbilities;
	}
	
	public void setName(String name) {
		mName = name;
	}
	
	public String getName() {
		return mName;
	}
	
	public void setDesc(String desc) {
		mDesc = desc;
	}
	
	public String getDesc() {
		return mDesc;
	}
	
	public void setSize(int size) {
		mSize = size;
	}
	
	public int getSize() {
		return mSize;
	}
	
	public void setMove(int move) {
		mMove = move;
	}
	
	public int getMove() {
		return mMove;
	}
	
	public void setColor(Color c) {
		mColor = c;
	}
	
	public Color getColor() {
		return mColor;
	}
	
	public void setThumb(Image im) {
		mThumb = im;
	}
	
	public Image getThumb() {
		return mThumb;
	}
	
	private Ability[] mAbilities = {};
	private String mName;
	private String mDesc;
	private int mSize;
	private int mMove;
	private Color mColor;
	private Image mThumb;
}
