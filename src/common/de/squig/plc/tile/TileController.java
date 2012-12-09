package de.squig.plc.tile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;
import de.squig.plc.PLC;
import de.squig.plc.event.ControllerDataEvent;
import de.squig.plc.event.PLCEvent;
import de.squig.plc.event.SearchEvent;
import de.squig.plc.event.SearchResponseEvent;
import de.squig.plc.event.SignalEvent;
import de.squig.plc.event.payloads.ControllerDataPayload;
import de.squig.plc.logic.BasicCircuit;
import de.squig.plc.logic.Circuit;
import de.squig.plc.logic.elements.CircuitElementNetworkData;
import de.squig.plc.logic.extender.ExtenderChannel;
import de.squig.plc.logic.helper.LogHelper;
import de.squig.plc.logic.objects.CircuitObject;
import de.squig.plc.logic.objects.LogicInput;
import de.squig.plc.logic.objects.LogicOutput;

public class TileController extends TilePLC implements IInventory {
	

	
	public final static int STATE_EDIT = 0;
	public final static int STATE_STOP = 1;
	public final static int STATE_RUN = 2;
	public final static int STATE_ERROR = 3;
	
	private int controllerID = -1;
	
	private Circuit circuit = null;
	public int machineState = -1;
	private String controllerName = "unknown";
	
	private int range = 32;
	
	
	public TileController() {
		super(PLCEvent.TARGETTYPE.CONTROLLER);
		LogHelper.info("Controler Constructor");
		machineState = TileController.STATE_EDIT;
		circuit = new BasicCircuit(this);
		
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if (circuit != null && circuit.getSimulator() != null)
			circuit.getSimulator().onTick(worldObj.getTotalWorldTime());
	}
	
	@Override
	protected void initialize() {
		super.initialize();
		LogHelper.info("Controler Init "+xCoord+" "+yCoord+" "+zCoord+" with uuid: "+uuid.toString());
		
		controllerName = "no name";
	}

	public void onEvent(PLCEvent event) {
		if (event instanceof SignalEvent) {
			SignalEvent events = (SignalEvent) event;
			CircuitObject obj = circuit.getByType(CircuitObject.TYPES.INPUT, events.getChannel()+"");
			if (obj instanceof LogicInput) {
				LogicInput inp = (LogicInput) obj;
				inp.onSignal(events.getSignal());
			}
		}
		else if (event instanceof ControllerDataEvent) {
			sendUpdatesToExtenders(true);
		} else if (event instanceof SearchEvent) {
			if (event.getSource().getWorldObj() == getWorldObj()) {
				int dx = xCoord - event.getSource().xCoord;
				int dy = yCoord - event.getSource().yCoord;
				int dz = zCoord - event.getSource().zCoord;
				int dist = (int)Math.floor(Math.sqrt(dx * dx + dy * dy + dz * dz));
				if (range >= dist) {
					SearchResponseEvent resp = new SearchResponseEvent(this, 
							event.getSource().getUuid(),
							dist,uuid,controllerName,
							circuit.getByType(CircuitObject.TYPES.INPUT).size(),
							circuit.getByType(CircuitObject.TYPES.OUTPUT).size());
					PLC.instance.getNetworkBroker().fireEvent(resp);
				}
			}
		}
		
	}
	
	
	
	@Override
	public void onChunkUnload() {
		circuit.onDestroy();
		super.onChunkUnload();
	}
	
	public void onDestroy() {
		circuit.onDestroy();
		super.onDestroy();
	}
	
	/**
     * The ItemStacks that hold the items currently being used in the Calcinator
     */
	
	
	private ItemStack[] calcinatorItemStacks = new ItemStack[3];

    public void readFromNBT(NBTTagCompound nbtTagCompound) {
		super.readFromNBT(nbtTagCompound);
		LogHelper.info("readFromNBT called");

		try {
			InputStream is = new ByteArrayInputStream(
					nbtTagCompound.getByteArray("elements"));
			DataInputStream dis = new DataInputStream(is);
			List<CircuitElementNetworkData> nd = circuit.loadElementsFrom(dis, true);
			circuit.injectElements(nd, true);
		} catch (IOException ex) {
			LogHelper.error("exception durring reading elements data!");
		}
	}

	public void writeToNBT(NBTTagCompound nbtTagCompound) {
		super.writeToNBT(nbtTagCompound);
		LogHelper.info("writeToNBT called");
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream w = new DataOutputStream(baos);
			circuit.saveElementsTo(w, true);
			w.flush();
			byte[] result = baos.toByteArray();
			nbtTagCompound.setByteArray("elements", result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		// Write the ItemStacks in the inventory to NBT
		/*NBTTagList tagList = new NBTTagList();
        for (int currentIndex = 0; currentIndex < this.calcinatorItemStacks.length; ++currentIndex) {
            if (this.calcinatorItemStacks[currentIndex] != null) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)currentIndex);
                this.calcinatorItemStacks[currentIndex].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }
        nbtTagCompound.setTag("Items", tagList);
        */
	}

	/**
     * Returns the number of slots in the inventory.
     */
	public int getSizeInventory() {
		return this.calcinatorItemStacks.length;
	}

	/**
     * Returns the stack in slot i
     */
	public ItemStack getStackInSlot(int i) {
		return this.calcinatorItemStacks[i];
	}

	public ItemStack decrStackSize(int i, int j) {
		// TODO Auto-generated method stub
		return null;
	}

	public ItemStack getStackInSlotOnClosing(int i) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2) {
		// TODO Auto-generated method stub

	}

	public String getInvName() {
		return "container.plcController"; //+ ModBlocks.CALCINATOR_NAME;
	}

	public int getInventoryStackLimit() {
		return 64;
	}

	public void openChest() { }
	public void closeChest() { }

	
	
	
	public Circuit getCircuit() {
		return circuit;
	}

	public void setCircuit(Circuit circuit) {
		this.circuit = circuit;
	}

	public int getMachineState() {
		return machineState;
	}

	public void setMachineState(int machineState) {
		this.machineState = machineState;
	}
	public String getControllerName() {
		return controllerName;
	}
	public void setControllerName(String controllerName) {
		this.controllerName = controllerName;
	}

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public void sendUpdatesToExtenders(boolean updateAll) {
		List<ControllerDataPayload> tosend = new ArrayList<ControllerDataPayload>();
		for (CircuitObject out : circuit.getByType(CircuitObject.TYPES.OUTPUT)) {
			LogicOutput lo = (LogicOutput) out;
			if (lo.isChanged() || updateAll) {
				tosend.add(new ControllerDataPayload(lo.getLinkNumberInt(), lo.getSignal()));
				lo.setChanged(false);
			}
		}
		if (tosend.size() > 0)
			PLC.instance.getNetworkBroker().fireEvent(new ControllerDataEvent(circuit.getController(), circuit.getController().getUuid(), tosend));
	}

	
	
	
	

}
