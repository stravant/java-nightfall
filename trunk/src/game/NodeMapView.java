package game;

import java.awt.Color;
import java.awt.Graphics;

import game.NodeMap.Node.Status;
import gui.*;
import gui.WidgetText.TextAlign;
import util.*;
import input.*;

public class NodeMapView extends WidgetRect {
	private interface IAction {
		public void onSelect();
		public void onDeselect();
		public void onClick(NodeMap.Node nd);
	}
	
	private class ActionPanOrSelectNode implements IAction {
		private void scrollify(Widget w, final Vec dir) {
			w.onMouseDown.connect(new MouseEvent.Listener() {
				public void onMouseEvent(MouseEvent e) {
					mScroll = dir;
				}
			});
			w.onMouseUp.connect(new MouseEvent.Listener() {
				public void onMouseEvent(MouseEvent e) {
					mScroll = new Vec(0, 0);
				}
			});
		}
		public ActionPanOrSelectNode() {
			mWidget = new Widget() {
				public void onRender(RenderTarget t) {
					mOriginOffset = mOriginOffset.add(mScroll);
				}
			};
			mWidget.setSize(new UDim(0, 0, 1, 1));
			mWidget.setActive(false);
			mWidget.setParent(NodeMapView.this);
			mWidget.setVisible(false);
			//
			WidgetImage scrollRight = new WidgetImage();
			scrollRight.setPos(new UDim(-64, -64, 1, 0.5f));
			scrollRight.setSize(new UDim(64, 128));
			scrollRight.setParent(mWidget);
			scrollRight.setImage(ResourceLoader.LoadImage("scroll-right.png"));
			scrollify(scrollRight, new Vec(MAP_SCROLL_SPEED, 0));
			//
			WidgetImage scrollLeft = new WidgetImage();
			scrollLeft.setPos(new UDim(0, -64, 0, 0.5f));
			scrollLeft.setSize(new UDim(64, 128));
			scrollLeft.setParent(mWidget);
			scrollLeft.setImage(ResourceLoader.LoadImage("scroll-left.png"));
			scrollify(scrollLeft, new Vec(-MAP_SCROLL_SPEED, 0));
			//
			WidgetImage scrollDown = new WidgetImage();
			scrollDown.setPos(new UDim(-64, -64, 0.5f, 1));
			scrollDown.setSize(new UDim(128, 64));
			scrollDown.setParent(mWidget);
			scrollDown.setImage(ResourceLoader.LoadImage("scroll-down.png"));
			scrollify(scrollDown, new Vec(0, MAP_SCROLL_SPEED));
			//
			WidgetImage scrollup = new WidgetImage();
			scrollup.setPos(new UDim(-64, 0, 0.5f, 0));
			scrollup.setSize(new UDim(128, 64));
			scrollup.setParent(mWidget);
			scrollup.setImage(ResourceLoader.LoadImage("scroll-up.png"));
			scrollify(scrollup, new Vec(0, -MAP_SCROLL_SPEED));
		}
		public void onSelect() {
			showGui();
		}
		public void onDeselect() {
			hideGui();
		}
		public void onClick(NodeMap.Node nd) {
			if (nd.NStatus != NodeMap.Node.Status.Unknown) {
				mActionViewNodeStats.setState(nd);
				setAction(mActionViewNodeStats);
			}
		}
		public void showGui() {
			mWidget.setVisible(true);
		}
		public void hideGui() {
			mWidget.setVisible(false);
		}
		
		private Vec mScroll = new Vec(0, 0);
		private Widget mWidget;
	}
	
	private class ActionViewNodeStats implements IAction {
		public ActionViewNodeStats() {
			mEnterBattleWidget = new WidgetRect();
			mEnterBattleWidget.setSize(new UDim(400, 400));
			mEnterBattleWidget.setPos(new UDim(-200, -200, 0.5f, 0.5f));
			mEnterBattleWidget.setColor(new Color(128, 128, 128, 200));
			mEnterBattleWidget.setParent(NodeMapView.this);
			mEnterBattleWidget.setVisible(false);
			//
			WidgetLabel nodename = new WidgetLabel();
			nodename.setSize(new UDim(0, 20, 1, 0));
			nodename.setBackground(false);
			nodename.setTextColor(Color.yellow);
			nodename.setParent(mEnterBattleWidget);
			mNodeNameWidget = nodename;
			//
			WidgetImage nodeimage = new WidgetImage();
			nodeimage.setSize(new UDim(64, 64));
			nodeimage.setPos(new UDim(20, -32, 0, 0.5f));
			nodeimage.setParent(mEnterBattleWidget);
			mNodeImageWidget = nodeimage;
			//
			WidgetText nodedesc = new WidgetText();
			nodedesc.setSize(new UDim(-124, 128, 1, 0));
			nodedesc.setPos(new UDim(104, -64, 0, 0.5f));
			nodedesc.setBackground(false);
			nodedesc.setTextColor(Color.yellow);
			nodedesc.setTextAlign(TextAlign.Left);
			nodedesc.setParent(mEnterBattleWidget);
			mNodeDescWidget = nodedesc;
			//
			WidgetLabel enter = new WidgetLabel();
			enter.setSize(new UDim(0, 30, 0.5f, 0));
			enter.setPos(new UDim(0, -30, 0, 1));
			enter.setColor(Color.red);
			enter.setTextColor(Color.black);
			enter.setText("ENTER NODE");
			enter.setParent(mEnterBattleWidget);
			WidgetPadding.applyPadding(enter, 4);
			enter.onMouseDown.connect(new MouseEvent.Listener() {
				public void onMouseEvent(MouseEvent e) {
					if (mTargetNode.DataBattle != null) {
						enterBattle();
					}
				}
			});
			//
			WidgetLabel cancel = new WidgetLabel();
			cancel.setSize(new UDim(0, 30, 0.5f, 0));
			cancel.setPos(new UDim(0, -30, 0.5f, 1));
			cancel.setColor(Color.red);
			cancel.setTextColor(Color.black);
			cancel.setText("CANCEL");
			cancel.setParent(mEnterBattleWidget);
			WidgetPadding.applyPadding(cancel, 4);
			cancel.onMouseDown.connect(new MouseEvent.Listener() {
				public void onMouseEvent(MouseEvent e) {
					setAction(mActionPanOrSelectNode);
				}
			});
		}
		public void onSelect() {
			showGui();
		}
		public void onDeselect() {
			hideGui();
		}
		public void onClick(NodeMap.Node nd) {
			
		}
		public void showGui() {
			mEnterBattleWidget.setVisible(true);
		}
		public void hideGui() {
			mEnterBattleWidget.setVisible(false);
		}
		public void setState(NodeMap.Node nd) {
			mTargetNode = nd;
			mNodeNameWidget.setText(nd.Name);
			mNodeImageWidget.setImage(nd.Image);
			mNodeDescWidget.setText(nd.Desc);
		}
		public void enterBattle() {
			NodeMapView.this.setVisible(false);
			mGame.enterDataBattle(mTargetNode.DataBattle, mTargetNode);
		}
		
		private NodeMap.Node mTargetNode;
		private WidgetRect mEnterBattleWidget;
		private WidgetLabel mNodeNameWidget;
		private WidgetImage mNodeImageWidget;
		private WidgetText mNodeDescWidget;
	}
	
	public NodeMapView(NodeMap target, GameSession game) {
		mTarget = target;
		mGame = game;
		//
		setColor(Color.black);
		//
		mActionPanOrSelectNode = new ActionPanOrSelectNode();
		mActionViewNodeStats = new ActionViewNodeStats();
		setAction(mActionPanOrSelectNode);
		//
		onMouseDown.connect(new MouseEvent.Listener() {
			public void onMouseEvent(MouseEvent e) {
				if (e.getButton() == MouseEvent.MouseButton.Left) {
					//find node to click
					Vec pos = e.getInput().getMousePos();
					for (NodeMap.Node nd : mTarget.getNodes()) {
						Vec ndPos = nd.Pos.mul(MAP_SCALE).add(mOriginOffset);
						if (pos.getX() > ndPos.getX()-32 && pos.getX() < ndPos.getX()+32
								&& pos.getY() > ndPos.getY()-32 && pos.getY() < ndPos.getY()+32) {
							mCurrentAction.onClick(nd);
						}
					}
				} else if (e.getButton() == MouseEvent.MouseButton.Right) {
					//start pan action
					mStartDragOriginOffset = mOriginOffset;
					mStartDragMousePos = e.getInput().getMousePos();
					mDragging = true;
				}
			}
		});
		onMouseUp.connect(new MouseEvent.Listener() {
			public void onMouseEvent(MouseEvent e) {
				mDragging = false;
			}
		});
		onMouseMove.connect(new MouseEvent.Listener() {
			public void onMouseEvent(MouseEvent e) {
				if (mDragging && mCurrentAction == mActionPanOrSelectNode) {
					Vec dif = mStartDragMousePos.sub(e.getInput().getMousePos());
					mOriginOffset = mStartDragOriginOffset.sub(dif);
				}
			}
		});
	}
	
	public void onRender(RenderTarget t) {
		super.onRender(t);
		Graphics g = t.getContext();
		//links
		for (NodeMap.Link ln : mTarget.getLinks()) {
			Vec apos = ln.NodeA.Pos.mul(MAP_SCALE).add(mOriginOffset);
			Vec bpos = ln.NodeB.Pos.mul(MAP_SCALE).add(mOriginOffset);
			if (ln.NodeA.NStatus == Status.Defeated || ln.NodeB.NStatus == Status.Defeated) {
				g.setColor(MAP_LINK_COLOR);
			} else {
				g.setColor(MAP_LINK_DARK_COLOR);
			}
			g.drawLine(apos.getX(), apos.getY(), bpos.getX(), bpos.getY());
		}
		
		//nodes
		for (NodeMap.Node nd : mTarget.getNodes()) {
			Vec pos = nd.Pos.mul(MAP_SCALE).add(mOriginOffset);
			switch (nd.NStatus) {
			case Unknown:
				g.setColor(MAP_LINK_DARK_COLOR);
				g.fillOval(pos.getX()-10, pos.getY()-5, 20, 10);
				break;
			case Visible:
				g.setColor(MAP_LINK_DARK_COLOR);
				g.fillOval(pos.getX()-10, pos.getY()-5, 20, 10);
				g.drawImage(nd.DarkImage, pos.getX()-32, pos.getY()-32, null);			
				break;
			case Defeated:
				g.setColor(MAP_LINK_COLOR);
				g.fillOval(pos.getX()-10, pos.getY()-5, 20, 10);
				g.drawImage(nd.Image, pos.getX()-32, pos.getY()-32, null);
				break;
			}
		}
	}
	
	public void enter() {
		setAction(mActionPanOrSelectNode);
	}
	
	private void setAction(IAction act) {
		if (mCurrentAction != null)
			mCurrentAction.onDeselect();
		mCurrentAction = act;
		if (mCurrentAction != null)
			mCurrentAction.onSelect();
	}
	
	private Vec mOriginOffset = new Vec(300, 300);
	private Vec mStartDragOriginOffset;
	private Vec mStartDragMousePos;
	private boolean mDragging = false;
	
	private IAction mCurrentAction;
	private ActionViewNodeStats mActionViewNodeStats;
	private ActionPanOrSelectNode mActionPanOrSelectNode;
	
	private final GameSession mGame;
	private final NodeMap mTarget;
	
	private static final Color MAP_LINK_DARK_COLOR = new Color(0, 128, 0);
	private static final Color MAP_LINK_COLOR = Color.green;
	private static final int MAP_SCALE = 128;
	private static final int MAP_SCROLL_SPEED = 8;
}
