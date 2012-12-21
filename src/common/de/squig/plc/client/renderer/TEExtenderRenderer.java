/**
 * 
 *  Contributors:
 *   
 *  Thunderdark - siding transformation and and rotation 
 *  from https://github.com/Thunderdark/ModularForceFieldSystem/blob/master/src/minecraft/chb/mods/mffs/client/TECapacitorRenderer.java
 * 
 */

package de.squig.plc.client.renderer;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import de.squig.plc.tile.TileExtender;



public class TEExtenderRenderer extends TileEntitySpecialRenderer {
	
	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y,
			double z, float f) {

		TileExtender ext = (TileExtender) tile;

		
		char[] ins = ext.getInputs();
		char[] outs = ext.getOutputs();
		
		GL11.glPushMatrix();

		// @TODO side impl

		int side = ext.getSide();

		float dx = 1F / 16;
		float dz = 1F / 16;
		float displayWidth = 1 - 2F / 16;
		float displayHeight = 1 - 2F / 16;
		GL11.glTranslatef((float) x, (float) y, (float) z);
		switch (side) {
		case 1:

			break;
		case 0:
			GL11.glTranslatef(1, 1, 0);
			GL11.glRotatef(180, 1, 0, 0);
			GL11.glRotatef(180, 0, 1, 0);

			break;
		case 3:
			GL11.glTranslatef(0, 1, 0);
			GL11.glRotatef(0, 0, 1, 0);
			GL11.glRotatef(90, 1, 0, 0);

			break;
		case 2:
			GL11.glTranslatef(1, 1, 1);
			GL11.glRotatef(180, 0, 1, 0);
			GL11.glRotatef(90, 1, 0, 0);

			break;
		case 5:
			GL11.glTranslatef(0, 1, 1);
			GL11.glRotatef(90, 0, 1, 0);
			GL11.glRotatef(90, 1, 0, 0);

			break;
		case 4:
			GL11.glTranslatef(1, 1, 0);
			GL11.glRotatef(-90, 0, 1, 0);
			GL11.glRotatef(90, 1, 0, 0);

			break;
		}
		GL11.glTranslatef(dx + displayWidth / 2, 1F, dz + displayHeight / 2);
		GL11.glRotatef(-90, 1, 0, 0);

		

		float topLeft = -0.4995f;

		float unit = 1 / 16f;
		float tu = 1/8f;
		
		
		
		
		
		GL11.glDisable(GL11.GL_LIGHTING);
		bindTextureByName("/ressources/art/txt/white.png");
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	
       /*
		GL11.glColor4f(1.0F, 0.0F, 0.0F, 1.0F);
	
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0,0);
		GL11.glVertex3f(topLeft + 15 * unit, topLeft + 14 * unit, 0.0001f); 
		GL11.glTexCoord2f(0,2*tu);																	// corner
		GL11.glVertex3f(topLeft + 15 * unit, topLeft + 15 * unit, 0.0001f);
		GL11.glTexCoord2f(2*tu,2*tu);																	// corner
		GL11.glVertex3f(topLeft + 14 * unit, topLeft + 15 * unit, 0.0001f);
		GL11.glTexCoord2f(2*tu,0);																	// corner
		GL11.glVertex3f(topLeft + 14 * unit, topLeft + 14 * unit, 0.0001f); 
		GL11.glEnd();
		*/
		
		
		for (int i = 0; i < ins.length; i++) {
			drawChannel((char)0, ins[i], i);	
		}
		for (int i = 0; i < outs.length; i++) {
			drawChannel((char)1, outs[i], i);	
		}
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDepthMask(true);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		//GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
		GL11.glPopMatrix();
	}
	
	private final float topLeft = -0.5f;
	private final float unit = 1/16f;
	
	private void drawChannel(char type, char status, int nmb) {
		//LogHelper.info((int)status+"");
		switch (status) {
			case 0:
				GL11.glColor3f(0.3F, 0.3F, 0.3F);
				break;
			case 1:
				GL11.glColor3f(0.0F, 0.0F, 0.5F);
				break;
			case 2:
				GL11.glColor3f(0.0F, 1.0F, 0.0F);
				break;
		}
		
		int xpos = (nmb / 8);
		int ypos = 7+(nmb % 8);
		
		float x = xpos;
		float y = 15-ypos;
		
		if (type == (char)0)
			x += 1;
		if (type == (char)1)
			x += 10;
		
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0,0);
		GL11.glVertex3f(topLeft + (x+1f) * unit, topLeft + y * unit, 0.0001f); 
		GL11.glTexCoord2f(0,1);																	// corner
		GL11.glVertex3f(topLeft + (x+1f) * unit, topLeft + (y+1f) * unit, 0.0001f);
		GL11.glTexCoord2f(1,1);																	// corner
		GL11.glVertex3f(topLeft + x * unit, topLeft + (y+1f) * unit, 0.0001f);
		GL11.glTexCoord2f(1,0);																	// corner
		GL11.glVertex3f(topLeft + x * unit, topLeft + y * unit, 0.0001f); 
		GL11.glEnd();
	}

	
}
