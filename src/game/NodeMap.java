package game;

import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.util.ArrayList;
import java.util.HashMap;

import json.*;
import util.*;

public class NodeMap {
	public static class Link {
		public Node NodeA, NodeB;
	}
	public static class Node {
		public static enum Type {
			Warez,
			Battle;
		}
		public static enum Status {
			Unknown,
			Visible,
			Defeated;
		}
		public void setDefeated() {
			NStatus = Status.Defeated;
			for (Node n : Adjacent) {
				if (n.NStatus == Status.Unknown)
					n.NStatus = Status.Visible;
			}
		}
		public int Id;
		public DataBattleInfo DataBattle;
		public Type NType;
		public Status NStatus;
		public String Name;
		public String Desc;
		public Image Image;
		public Image DarkImage;
		public Vec Pos;
		public ArrayList<Node> Adjacent = new ArrayList<Node>();
	}
	
	public NodeMap(GameSession session) {
		mGame = session;
	}
	
	public void loadFrom(JSNode node) {
		JSObject nodeo = (JSObject)node;
		
		//nodes
		for (JSNode gamenode : ((JSArray)nodeo.get("nodes")).getChildren()) {
			JSObject gamenodeo = (JSObject)gamenode;
			Node nd = new Node();
			nd.Id = gamenodeo.get("id").getIntValue();
			nd.Name = gamenodeo.get("name").getStringValue();
			nd.Desc = gamenodeo.get("desc").getStringValue();
			//
			nd.Image = ResourceLoader.LoadImage(gamenodeo.get("image").getStringValue());
			BufferedImage tmpImg = new BufferedImage(nd.Image.getWidth(null), nd.Image.getHeight(null), 
													BufferedImage.TYPE_INT_ARGB);
			tmpImg.getGraphics().drawImage(nd.Image, 0, 0, null);
			ColorSpace grayColorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
			ColorConvertOp op = new ColorConvertOp(grayColorSpace, tmpImg.getColorModel().getColorSpace(), null);
			op.filter(tmpImg, tmpImg);
			nd.DarkImage = tmpImg;
			//
			nd.Pos = JSUtil.toFVec(gamenodeo.get("pos")).vec();
			JSNode battlename = gamenodeo.get("battle");
			if (battlename == null) {
				nd.NType = Node.Type.Warez;
			} else {
				nd.NType = Node.Type.Battle;
				nd.DataBattle = mGame.getDataBattleLibrary().getDataBattleByName(battlename.getStringValue());
			}
			nd.NStatus = Node.Status.Unknown;
			mNodes.add(nd);
			mNodesById.put(nd.Id, nd);
		}
		
		//links
		for (JSNode link : ((JSArray)nodeo.get("links")).getChildren()) {
			JSArray arr = (JSArray)link;
			Link l = new Link();
			Node na = mNodesById.get(arr.get(0).getIntValue());
			Node nb = mNodesById.get(arr.get(1).getIntValue());
			l.NodeA = na;
			l.NodeB = nb;
			na.Adjacent.add(nb);
			nb.Adjacent.add(na);
			mLinks.add(l);
		}
	}
	
	public Node getNodeById(int id) {
		return mNodesById.get(id);
	}
	
	public ArrayList<Node> getNodes() {
		return mNodes;
	}
	
	public ArrayList<Link> getLinks() {
		return mLinks;
	}
	
	private final GameSession mGame;
	
	private HashMap<Integer, Node> mNodesById = new HashMap<Integer, Node>();
	private ArrayList<Node> mNodes = new ArrayList<Node>();
	private ArrayList<Link> mLinks = new ArrayList<Link>();
}
