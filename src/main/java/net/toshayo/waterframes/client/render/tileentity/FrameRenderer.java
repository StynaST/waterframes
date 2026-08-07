package net.toshayo.waterframes.client.render.tileentity;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.toshayo.waterframes.WFConfig;
import net.toshayo.waterframes.WaterFramesMod;
import net.toshayo.waterframes.client.TextureDisplay;
import net.toshayo.waterframes.tileentities.DisplayTileEntity;
import org.lwjgl.opengl.GL11;

public class FrameRenderer extends DisplayRenderer {
    private static final float[] SIDE_SHADE = { 0.5F, 1.0F, 0.8F, 0.8F, 0.6F, 0.6F };

    private RenderBlocks renderBlocks;

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
        if(tileEntity.getWorldObj() == null || renderBlocks == null) {
            return;
        }

        DisplayTileEntity tile = (DisplayTileEntity) tileEntity;
        if(tile.isVisible()) {
            Tessellator tessellator = Tessellator.instance;
            this.bindTexture(TextureMap.locationBlocksTexture);
            RenderHelper.disableStandardItemLighting();
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_CULL_FACE);
            if (Minecraft.isAmbientOcclusionEnabled()) {
                GL11.glShadeModel(GL11.GL_SMOOTH);
            } else {
                GL11.glShadeModel(GL11.GL_FLAT);
            }

            tessellator.startDrawingQuads();
            tessellator.setTranslation(x - (float) tile.xCoord, y - (float) tile.yCoord, z - (float) tile.zCoord);
            tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);

            WaterFramesMod.FRAME.setBlockBoundsBasedOnState(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
            renderBlocks.setRenderBoundsFromBlock(WaterFramesMod.FRAME);
            this.renderFrame(tile);

            tessellator.setTranslation(0.0, 0.0, 0.0);
            tessellator.draw();
            RenderHelper.enableStandardItemLighting();
        }

        super.renderTileEntityAt(tileEntity, x, y, z, partialTicks);
    }

    /**
     * The side of the frame the picture is drawn on is opaque and almost black, so it shows up behind
     * every transparent pixel of the picture. That side is left out while something is being displayed,
     * a picture without transparency covers it completely anyway.
     */
    private void renderFrame(DisplayTileEntity tile) {
        final Block block = WaterFramesMod.FRAME;
        final World world = tile.getWorldObj();
        final int x = tile.xCoord, y = tile.yCoord, z = tile.zCoord;

        int hiddenSide = this.isDisplayingContent(tile) ? this.displayedSide(tile) : -1;
        if (hiddenSide == -1) {
            renderBlocks.renderStandardBlock(block, x, y, z);
            return;
        }

        Tessellator tessellator = Tessellator.instance;
        tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));

        for (int side = 0; side < 6; side++) {
            if (side == hiddenSide) {
                continue;
            }

            float shade = SIDE_SHADE[side];
            tessellator.setColorOpaque_F(shade, shade, shade);
            IIcon icon = renderBlocks.getBlockIcon(block, world, x, y, z, side);

            switch (side) {
                case 0:
                    renderBlocks.renderFaceYNeg(block, x, y, z, icon);
                    break;
                case 1:
                    renderBlocks.renderFaceYPos(block, x, y, z, icon);
                    break;
                case 2:
                    renderBlocks.renderFaceZNeg(block, x, y, z, icon);
                    break;
                case 3:
                    renderBlocks.renderFaceZPos(block, x, y, z, icon);
                    break;
                case 4:
                    renderBlocks.renderFaceXNeg(block, x, y, z, icon);
                    break;
                case 5:
                    renderBlocks.renderFaceXPos(block, x, y, z, icon);
                    break;
            }
        }
    }

    private int displayedSide(DisplayTileEntity tile) {
        int meta = tile.getBlockMetadata();
        // FrameBlock#getIcon swaps the west and east sides before comparing them to the metadata
        if (meta == 4) {
            return 5;
        }
        if (meta == 5) {
            return 4;
        }
        return meta;
    }

    private boolean isDisplayingContent(DisplayTileEntity tile) {
        if (!WFConfig.keepsRendering()) {
            return false;
        }
        TextureDisplay display = tile.requestDisplay();
        return display != null && display.canRender();
    }

    @Override
    public void func_147496_a(World world) {
        renderBlocks = new RenderBlocks(world);
    }

    @Override
    public EnumFacing direction(DisplayTileEntity tile) {
        return EnumFacing.getFront(tile.getBlockMetadata());
    }
}
