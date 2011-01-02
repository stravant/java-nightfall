package game;

import java.awt.Color;

import util.UDim;

import game.Agent.TurnState;
import game.AgentInfo.Ability;
import game.NodeMap.Node;
import gui.WidgetRoot;
import json.*;

public class GameSession {
	public GameSession() {
		try {
			//// load the agent library
			mAgentLibrary.addAbilitySource("BasicAttack", new AgentLibrary.AbilitySource() {
				public Ability loadAbility(JSNode nd) {
					JSObject obj = (JSObject)nd;
					String name = obj.get("name").getStringValue();
					String desc = obj.get("desc").getStringValue();
					int dmg = (int)obj.get("damage").getNumberValue();
					int range = (int)obj.get("range").getNumberValue();
					return new AgentInfo.AbilityDamageGeneric(name, desc, range, dmg);
				}
			});
			mAgentLibrary.addAgents(ResourceLoader.LoadJSON("AgentData.json"));
			
			//// load the data battle library
			mDataBattleLibrary.addDataBattles(ResourceLoader.LoadJSON("BattleData.json"), 
												mAgentLibrary);
			
			//// load node map
			mNodeMap = new NodeMap(this);
			mNodeMap.loadFrom(ResourceLoader.LoadJSON("NodeData.json"));
			
			//// load the user's saved agents
			JSObject userdata = (JSObject)JSNode.parse(ResourceLoader.LoadData("SaveData.json"));
			JSArray useragents = (JSArray)((JSObject)userdata.get("inventory")).get("agents");
			//agents
			for (JSNode nd : useragents.getChildren()) {
				JSObject agentnd = (JSObject)nd;
				int quantity = (int)agentnd.get("quantity").getNumberValue();
				AgentInfo agent = mAgentLibrary.getAgentByName(agentnd.get("name").getStringValue());
				for (int i = 0; i < quantity; ++i)
					mInventory.addAgent(agent);
			}
			//defeated nodes
			for (JSNode nd : ((JSArray)userdata.get("defeated")).getChildren()) {
				mNodeMap.getNodeById(nd.getIntValue()).setDefeated();
			}
			//having defeated node 0? Set it as visible
			NodeMap.Node nd = mNodeMap.getNodeById(0);
			if (nd.NStatus == Node.Status.Unknown) {
				nd.NStatus = Node.Status.Visible;
			}
		} catch (Exception e) {
			System.err.println("Failed to load game data because: " + e.getMessage());
			e.printStackTrace();
		} 
	}
	
	public void enterDataBattle(DataBattleInfo info, NodeMap.Node nd) {
		DataBattle battle = new DataBattle(info, nd);
		//
		Team myTeam = new Team();
		myTeam.setColor(Color.blue);
		Team aiTeam = new Team();
		aiTeam.setColor(Color.red);
		//
		battle.addTeam(myTeam);
		battle.addTeam(aiTeam);
		//
		AIController ai = new AIController(battle, aiTeam);
		//
		for (DataBattleInfo.UnitEntry uent : info.getUnits()) {
			Agent a = new Agent(battle, aiTeam, uent.Pos, uent.Info);
			a.setTurnState(TurnState.Done);
			a.setAILogic(new AIStrategy.BasicAttackAIStrategy());
		}
		//
		mDataBattleView = new DataBattleView(battle, myTeam, this, ai);
		mDataBattleView.setSize(new UDim(0, 0, 1, 1));
		mDataBattleView.setParent(mGuiRoot);
	}
	
	public void enterNodeMap() {
		if (mNodeMapView == null) {
			mNodeMapView = new NodeMapView(mNodeMap, this);
			mNodeMapView.setSize(new UDim(0, 0, 1, 1));
			mNodeMapView.setParent(mGuiRoot);
		}
		mNodeMapView.enter();
		mNodeMapView.setVisible(true);
	}
	
	public Inventory getInventory() {
		return mInventory;
	}
	
	public AgentLibrary getAgentLibrary() {
		return mAgentLibrary;
	}
	
	public DataBattleLibrary getDataBattleLibrary() {
		return mDataBattleLibrary;
	}
	
	public void setGuiRoot(WidgetRoot r) {
		mGuiRoot = r;
	}
	
	//guis
	private WidgetRoot mGuiRoot;
	private DataBattleView mDataBattleView;
	private NodeMapView mNodeMapView;
	
	//internal data
	private NodeMap mNodeMap;
	private DataBattleLibrary mDataBattleLibrary = new DataBattleLibrary();
	private AgentLibrary mAgentLibrary = new AgentLibrary();
	private Inventory mInventory = new Inventory();
}
