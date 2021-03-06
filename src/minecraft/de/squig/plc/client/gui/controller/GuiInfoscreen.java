package de.squig.plc.client.gui.controller;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import de.squig.plc.client.gui.controlls.TextureButton;
import de.squig.plc.logic.elements.CircuitElement;
import de.squig.plc.logic.elements.Deleted;
import de.squig.plc.logic.elements.Line;
import de.squig.plc.logic.elements.functions.ElementFunction;
import de.squig.plc.logic.objects.guiFunctions.GuiFunction;
import de.squig.plc.logic.objects.guiFunctions.GuiFunctionIntDisplay;
import de.squig.plc.logic.objects.guiFunctions.GuiFunctionIntValue;
import de.squig.plc.logic.objects.guiFunctions.GuiFunctionTime;

public class GuiInfoscreen {

	private GuiController guiController;
	private List<GuiButton> controlls;
	private List<GuiFunctionImpl> guiFunctions = new LinkedList<GuiFunctionImpl>();;

	private FontRenderer fontRenderer;
	private int x, y, width, height;
	private boolean LinkNmbr = false;

	public GuiInfoscreen(GuiController guiController, CircuitElement element) {
		this.guiController = guiController;
		controlls = new ArrayList<GuiButton>();
		fontRenderer = guiController.getFontRenderer();
		x = guiController.infoX;
		y = guiController.infoY;
		width = guiController.infoWidth;
		height = guiController.infoHeight;
		int xx = 0;
		short id = 0;
		if (element == null || element instanceof Deleted) {
			
			for (Class ele : CircuitElement.getElements()) {
				if (ele != null && ele != Deleted.class) {
					try {
						Method method = ele.getMethod("getDisplayName");
						String name = (String) method.invoke(null);
						method = ele.getMethod("getDisplayTextureId");
						int txtId = (Integer) method.invoke(null);
						TextureButton btn = new TextureButton(0, x + 10
								+ (xx % 8) * 16, y + 30 + (xx / 8) * 16, txtId,
								false, name, id);
						controlls.add(btn);
						xx++;
					} catch (Exception e) {
					}
				}
				id++;
			}
		} else {
			if (element != null && !(element instanceof Deleted)) {
				LinkNmbr = element.getLinkNumber() != null && !element.getLinkNumber().equals("");
				TextureButton btn = new TextureButton(0, x + 133, y + 10, 240,
						false, "Delete this Element",-1);
				controlls.add(btn);
				if (element instanceof Line) {
					btn = new TextureButton(0, x + 116, y + 10, 48,
							false, "Switch Line connections",-2);
					controlls.add(btn);
				}
				
				for (ElementFunction fnct : element.getFunctions()) {
					 btn = new TextureButton(0, x + 10
							+ (xx % 8) * 16, y + 30 + (xx / 8) * 16, fnct.getTag().txtId,
							fnct.equals(element.getFunction()), fnct.getDescription(), fnct.getId());
					controlls.add(btn);
					xx++;
				}
				
				int yy = y+60;
				if (element.getLinkedObject() != null && element.getLinkedObject().getGuiFunctions() != null) {
				
					for (GuiFunction fnct :  element.getLinkedObject().getGuiFunctions()) {
						if (fnct instanceof GuiFunctionTime) {
							GuiFunctionImpl fncti = new GuiFunctionTimeImpl(guiController,this,fnct,element.getLinkedObject(),yy);
							guiFunctions.add(fncti);
							yy += fncti.getHeight();
						} else if (fnct instanceof GuiFunctionIntValue) {
							GuiFunctionImpl fncti = new GuiFunctionIntValueImpl(guiController,this,fnct,element.getLinkedObject(),yy);
							guiFunctions.add(fncti);
							yy += fncti.getHeight();
						} else if (fnct instanceof GuiFunctionIntDisplay) {
							GuiFunctionImpl fncti = new GuiFunctionIntDisplayImpl(guiController,this,fnct,element.getLinkedObject(),yy);
							guiFunctions.add(fncti);
							yy += fncti.getHeight();
						}
					}
				}
			
				
				
				
			}
		}
		guiController.getControllList().addAll(controlls);
	}

	public void drawForeground(int i, int j) {
		CircuitElement element = guiController.getSelectedElement();
		if (element != null && !(element instanceof Deleted)) {
			fontRenderer.drawString(element.getName(), x + 10, y + 10, 0x000000);
			if (LinkNmbr) {
				fontRenderer.drawString("Link: "+element.getLinkNumber(), x + 80, y + 10, 0x000000);
			 if (i >= x+80 && i < x+115)
		        	if (j >= y+10&& j < y+20) {
		        		int width = guiController.getFontRenderer().getStringWidth("Press 0-9 to change a Linked Logic");
		        		int xx = this.guiController.infoX+55 +3;
		        		int yy = this.y -12;
		        		int xcorr = 0;
		        		if (xx+width > 450)
		        			xx = xx+(450-(xx+width));
		        		guiController.drawGradientRect(xx-2+xcorr,yy-2, xx+width+2+xcorr, yy+8+2, 0xc0000000, 0xf0000000);
		        		guiController.getFontRenderer().drawString("Press 0-9 to change a Linked Logic", xx+xcorr,yy, 0xffffff);
		        	}
			}
		}
		else {
			fontRenderer.drawString("Insert an Element:", x + 10, y + 10,
					0x000000);
		}
		
		for (GuiFunctionImpl fnct : guiFunctions)
			fnct.drawForeground(i,j);

	}

	public void drawBackground() {

		for (int x = 0; x < width / 16; x++) {
			for (int y = 0; y < height / 16; y++) {
				int tx = 16;
				int ty = 16;
				if (x == 0)
					tx = 0;
				if (y == 0)
					ty = 0;
				if (x == (width - 1) / 16)
					tx = 32;
				if (y == (height - 1) / 16)
					ty = 32;
				int ssx = this.x + 16 * x;
				int ssy = this.y + 16 * y;

				if (tx == 16 && ty >= 16)
					tx = tx + 48;
				if (tx == 32 && ty >= 16)
					tx = tx + 48;

				guiController.drawTexturedModalRect(ssx, ssy, tx, ty, 16, 16);
			}
		}
		
		for (GuiFunctionImpl fnct : guiFunctions)
			fnct.drawBackground();

	}
	
	
	public boolean onActionPerformed(GuiButton btn) {
		
		if (controlls.contains(btn)) {
			CircuitElement element = guiController.getSelectedElement();
			if (btn instanceof TextureButton) {
				int id = ((TextureButton) btn).getId();
				if (id == -1) {
					guiController.tryConvert(Deleted.class, element);	
					guiController.refreshInfoScreen();
					return true;
				} else if (id == -2) {
					element.functionCycle();
					return true;
				}
				else if (element == null || element instanceof Deleted) {
					guiController.tryConvert(CircuitElement.getElements()[id], element);	
					guiController.refreshInfoScreen();
					return true;
				} else {
					element.setFunction(ElementFunction.getById(id));
					guiController.sendUpdate(false);
					guiController.refreshInfoScreen();
					return true;
				}
			}
		} else {
			for (GuiFunctionImpl fnct : guiFunctions) {
				if (fnct.actionPerformed(btn))
					return true;
			}

		}
		return false;
	}

	public void onClose() {
		guiController.getControllList().removeAll(controlls);
		for (GuiFunctionImpl fnct : guiFunctions)
			fnct.onClose();
	}

	public void drawCenteredString(FontRenderer par1FontRenderer,
			String par2Str, int par3, int par4, int par5) {
		guiController.drawCenteredString(par1FontRenderer, par2Str, par3, par4,
				par5);
	}

	public void drawString(FontRenderer par1FontRenderer, String par2Str,
			int par3, int par4, int par5) {
		guiController.drawString(par1FontRenderer, par2Str, par3, par4, par5);
	}

	public void drawTexturedModalRect(int par1, int par2, int par3, int par4,
			int par5, int par6) {
		guiController.drawTexturedModalRect(par1, par2, par3, par4, par5, par6);
	}

}
