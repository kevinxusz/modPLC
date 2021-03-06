package de.squig.plc.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import de.squig.plc.logic.Circuit;
import de.squig.plc.logic.CircuitStateNetworkData;
import de.squig.plc.logic.PoweredMapNetworkData;
import de.squig.plc.logic.elements.CircuitElementNetworkData;
import de.squig.plc.logic.objects.CircuitObjectNetworkData;
import de.squig.plc.tile.TileController;

public class PacketControllerData extends PLCPacket {
	private static enum FLAGS {NAME,STATE, ELEMENTS, ELEMENTSALL, OBJECTS, OBJECTSALL, POWERED};
	public int x, y, z;
	

	private List<CircuitObjectNetworkData> inObjects = null;
	private List<CircuitElementNetworkData>  inElements = null;
	private CircuitStateNetworkData inState = null;
	private PoweredMapNetworkData inPowered = null;
	
	private Circuit circuit  = null;
	
	private boolean dataState = false;
	private boolean dataElements = false;
	private boolean dataElementsAll = false;
	private boolean dataObjects = false;
	private boolean dataObjectsAll = false;
	private boolean dataPowered = false;
	
	private String controllerName = null;
	
	public PacketControllerData() {
		super(PacketTypeHandler.CIRCUITDATA, true);
	}

	public void setCoords(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void setCircuit (Circuit circuit) {
		this.circuit = circuit;
	}


	// int x,y,z
	// String id
	//
	// bool state
	// state...
	// bool elements 
	// bool elementsAll
	// elements....
	// bool objects
	// objects...
	// bool poweredState
	// poweredMap...
	// bool more
	//
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeInt(x);
		data.writeInt(y);
		data.writeInt(z);
		
		short flags = 0;
		
		if (controllerName != null)
			flags += 1 << FLAGS.NAME.ordinal();
		if (dataState) 
			flags += (1 << FLAGS.STATE.ordinal()) ;
		if (dataElements) 
			flags += (1 << FLAGS.ELEMENTS.ordinal()) ;
		if (dataElementsAll) 
			flags += (1 << FLAGS.ELEMENTSALL.ordinal());
		if (dataObjects) 
			flags += (1 << FLAGS.OBJECTS.ordinal());
		if (dataObjectsAll) 
			flags += (1 << FLAGS.OBJECTSALL.ordinal());
		if (dataPowered) 
			flags += (1 << FLAGS.POWERED.ordinal());
		
		data.writeShort(flags);
		
		if (controllerName != null) 
			data.writeUTF(controllerName);
		if (dataState) {
			circuit.saveStateTo(data);
		}
		
		if (dataElements) {
			circuit.saveElementsTo(data, dataElementsAll);
		}
	
		if (dataObjects) {
			circuit.saveObjectsTo(data, dataObjectsAll);
		}

		if (dataPowered) {
			circuit.savePoweredTo(data);	
		}
		
	}

	public void readData(DataInputStream data) throws IOException {
		this.x = data.readInt();
		this.y = data.readInt();
		this.z = data.readInt();
		short flags = data.readShort();
		
		
		boolean hasName = ((1 << FLAGS.NAME.ordinal()) & flags) > 0;
		dataState = ((1 << FLAGS.STATE.ordinal()) & flags) > 0;
		dataElements = ((1 << FLAGS.ELEMENTS.ordinal()) & flags) > 0;
		dataElementsAll = ((1 << FLAGS.ELEMENTSALL.ordinal()) & flags) > 0;
		dataObjects = ((1 << FLAGS.OBJECTS.ordinal()) & flags) > 0;
		dataObjectsAll = ((1 << FLAGS.OBJECTSALL.ordinal()) & flags) > 0;
		dataPowered = ((1 << FLAGS.POWERED.ordinal()) & flags) > 0;
		
		
		if (hasName)
			controllerName = data.readUTF();
		else controllerName = null;
		
		if (dataState) {
			inState = Circuit.loadStateFrom(data);
		}
		if (dataElements) {
			inElements = Circuit.loadElementsFrom(data, dataElementsAll);
		}
		if (dataObjects) {
			inObjects = Circuit.loadObjectsFrom(data, dataObjectsAll);
		}
		if (dataPowered) {
			inPowered = Circuit.loadPoweredFrom(data);
		}
	}

	public void execute(INetworkManager manager, Player player) {
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		World worldObj = null;
		if (side == Side.CLIENT) {
			if (player instanceof EntityClientPlayerMP)
				worldObj = ((EntityClientPlayerMP) player).worldObj;
		} else if (side == Side.SERVER) {
			
			if (player instanceof EntityPlayerMP)
				worldObj = ((EntityPlayerMP) player).worldObj;
		}

		if (worldObj != null) {
			TileEntity tile = worldObj.getBlockTileEntity(x, y, z);
			if (tile != null && tile instanceof TileController) {
				TileController tileController = (TileController) tile;
				
				if (controllerName != null)
					tileController.setControllerName(controllerName);
				
				Circuit circ = tileController.getCircuit();
				if (dataState && inState != null)
					circ.injectState(inState);
				if (dataElements && inElements != null)
					circ.injectElements(inElements, dataElementsAll);
				if (dataObjects && inObjects != null)
					circ.injectObjects(inObjects, dataElementsAll);
				if (dataPowered && inPowered != null) {
					circ.injectPowered(inPowered);
				}
				if (side == Side.SERVER) {
					PacketControllerData.updateArround(tileController,8);
				}
				circ.getMap().removeDeleted();
				circ.setNeedsSimulation(true);
			}
			
		}
	}


	public boolean isDataState() {
		return dataState;
	}

	public void setDataState(boolean dataState) {
		this.dataState = dataState;
	}

	public boolean isDataElements() {
		return dataElements;
	}

	public void setDataElements(boolean dataElements) {
		this.dataElements = dataElements;
	}

	public boolean isDataElementsAll() {
		return dataElementsAll;
	}

	public void setDataElementsAll(boolean dataElementsAll) {
		this.dataElementsAll = dataElementsAll;
	}

	public boolean isDataObjects() {
		return dataObjects;
	}

	public void setDataObjects(boolean dataObjects) {
		this.dataObjects = dataObjects;
	}

	public boolean isDataObjectsAll() {
		return dataObjectsAll;
	}

	public void setDataObjectsAll(boolean dataObjectsAll) {
		this.dataObjectsAll = dataObjectsAll;
	}

	public boolean isDataPowered() {
		return dataPowered;
	}

	public void setDataPowered(boolean dataPowered) {
		this.dataPowered = dataPowered;
	}

	public Circuit getCircuit() {
		return circuit;
	}		
	
	
	
	public static void sendElements(TileController tile,
			 EntityPlayer player, boolean purge) {
		
		
		PacketControllerData pkg = new PacketControllerData();
		pkg.setCircuit(tile.getCircuit());
		pkg.setDataElements(true);
		if (purge)
			pkg.setDataElementsAll(true);
		pkg.setDataObjects(true);
		pkg.setCoords(tile.xCoord, tile.yCoord, tile.zCoord);
		pkg.setControllerName(tile.getControllerName());
		Packet packet = PacketTypeHandler.populatePacket(pkg);

		Side side = FMLCommonHandler.instance().getEffectiveSide();

		if (side == Side.SERVER) {
			// Server
			EntityPlayerMP playermp = (EntityPlayerMP) player;
			PacketDispatcher.sendPacketToPlayer(packet, (Player) playermp);
		
		} else if (side == Side.CLIENT) {
			// Client
			//PacketDispatcher.sendPacketToServer(packet);
			EntityClientPlayerMP playerclient = (EntityClientPlayerMP)player;
			playerclient.sendQueue.addToSendQueue(packet);
		}

	}
	private static void updateArround(TileController tile, int i) {
		PacketControllerData pkg = new PacketControllerData();
		pkg.setCircuit(tile.getCircuit());
		pkg.setControllerName(tile.getControllerName());
		pkg.setDataElements(true);
		pkg.setDataElementsAll(false);
		pkg.setCoords(tile.xCoord, tile.yCoord, tile.zCoord);
		Packet packet = PacketTypeHandler.populatePacket(pkg);
		PacketDispatcher.sendPacketToAllAround(tile.xCoord, tile.yCoord, tile.zCoord, i,tile.getWorldObj().getWorldInfo().getDimension() , packet);	
	}
	
	public static void updateArroundWithPowermap(TileController tile, int i) {
		PacketControllerData pkg = new PacketControllerData();
		pkg.setCircuit(tile.getCircuit());
		pkg.setDataPowered(true);
		pkg.setDataObjects(true);
		pkg.setCoords(tile.xCoord, tile.yCoord, tile.zCoord);
		Packet packet = PacketTypeHandler.populatePacket(pkg);
		
		PacketDispatcher.sendPacketToAllAround(tile.xCoord, tile.yCoord, tile.zCoord, i,tile.getWorldObj().getWorldInfo().getDimension() , packet);	
	}

	public String getControllerName() {
		return controllerName;
	}

	public void setControllerName(String controllerName) {
		this.controllerName = controllerName;
	}
	
	
	
}