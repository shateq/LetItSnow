package shateq.mods.fabric.letitsnow.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shateq.mods.fabric.letitsnow.LetItSnowMod;

import java.util.Random;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow @Final private static Identifier SNOW;
    @Shadow @Final private MinecraftClient client;
    @Shadow
    private ShaderEffect transparencyShader;
    @Shadow private int ticks;
    @Shadow @Final private float[] field_20794;
    @Shadow @Final private float[] field_20795;

    @Inject(method = "render", at = @At("TAIL"))
    public void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        Profiler profiler = this.client.world.getProfiler();
        Vec3d vec3d = camera.getPos();
        double x = vec3d.getX();
        double y = vec3d.getY();
        double z = vec3d.getZ();

        if (this.transparencyShader != null) {
            RenderPhase.WEATHER_TARGET.startDrawing();
            profiler.swap("weather");
            RenderPhase.WEATHER_TARGET.endDrawing();
            this.renderSnow(lightmapTextureManager, tickDelta, x, y, z);
            this.transparencyShader.render(tickDelta);
            this.client.getFramebuffer().beginWrite(false);
        } else {
            RenderSystem.depthMask(false);
            profiler.swap("weather");
            this.renderSnow(lightmapTextureManager, tickDelta, x, y, z);
            RenderSystem.depthMask(true);
        }
    }


    public void renderSnow(LightmapTextureManager manager, float tickDelta, double d, double e, double f) {
        if(LetItSnowMod.enabled) {
            World world = this.client.world;
            assert world != null;
            float rainGradient = world.getRainGradient(tickDelta);
            int x = MathHelper.floor(d);
            int y = MathHelper.floor(e);
            int z = MathHelper.floor(f);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder builder = tessellator.getBuffer();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            int that = -1;
            int distance = MinecraftClient.isFancyGraphicsOrBetter() ? 10 : 5;
            RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
            float tickTime = (float)this.ticks + tickDelta; // As 'n'

            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            for(int zLoop = z - distance; zLoop <= z + distance; ++zLoop) {
                for(int xLoop = x - distance; xLoop <= x + distance; ++xLoop) {
                    int question = (zLoop - z + 16) * 32 + xLoop - x + 16;
                    double direction1 = field_20794[question] * 0.5D;
                    double direction2 = field_20795[question] * 0.5D;

                    mutable.set(xLoop, y, zLoop);
                    int top = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, mutable).getY();

                    int yDiff = y - distance;
                    int ySum = y + distance;
                    if(yDiff < top) {
                        yDiff = top;
                    }

                    if (ySum < top) {
                        ySum = top;
                    }

                    int startingHeight = top;
                    if(top < y) {
                        startingHeight = y;
                    }

                    // Main if
                    if(yDiff != ySum) {
                       Random random = new Random((long) xLoop * xLoop * 3121 + xLoop * 45238971L ^ (long) zLoop * zLoop * 418711 + zLoop * 13761L);
                       mutable.set(xLoop, yDiff, zLoop);

                       float notExact;
                       float _alpha;

                       if (that >= 0) {
                           tessellator.draw();
                       }
                       that = 1;

                       RenderSystem.setShaderTexture(0, SNOW);
                       builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);

                        float minus = -((float)(this.ticks & 511) + tickDelta) / 512.0F;
                        notExact = (float)(random.nextDouble() + (double)tickTime * 0.01D * (double)((float)random.nextGaussian()));
                        float gaussian = (float)(random.nextDouble() + (double)(tickTime * (float)random.nextGaussian()) * 0.001D);

                        double halfX = (double)xLoop + 0.5D - d;
                        double halfZ = (double)zLoop + 0.5D - f;

                        _alpha = (float)Math.sqrt(halfX * halfX + halfZ * halfZ) / (float)distance;
                        float alpha = ((1.0F - _alpha * _alpha) * 0.3F + 0.5F) * rainGradient;
                        mutable.set(xLoop, startingHeight, zLoop);
                        int lightmapCoordinates = WorldRenderer.getLightmapCoordinates(world, mutable);

                        int light1 = lightmapCoordinates >> 16 & '\uffff';
                        int light2 = lightmapCoordinates & '\uffff';
                        int secondLight = (light1 * 3 + 240) / 4;
                        int firstLight = (light2 * 3 + 240) / 4;

                        builder.vertex((double)xLoop - d - direction1 + 0.5D, (double)ySum - e, (double)zLoop - f - direction2 + 0.5D)
                                .texture(0.0F + notExact, (float)ySum * 0.25F + minus + gaussian)
                                .color(1.0F, 1.0F, 1.0F, alpha).light(firstLight, secondLight).next();

                        builder.vertex((double)xLoop - d + direction1 + 0.5D, (double)ySum - e, (double)zLoop - f + direction2 + 0.5D)
                                .texture(1.0F + notExact, (float)ySum * 0.25F + minus + gaussian)
                                .color(1.0F, 1.0F, 1.0F, alpha)
                                .light(firstLight, secondLight).next();

                        builder.vertex((double)xLoop - d + direction1 + 0.5D, (double)ySum - e, (double)zLoop - f + direction2 + 0.5D)
                                .texture(1.0F + notExact, (float)ySum * 0.25F + minus + gaussian)
                                .color(1.0F, 1.0F, 1.0F, alpha)
                                .light(firstLight, secondLight).next();

                        builder.vertex((double)xLoop - d - direction1 + 0.5D, (double)ySum - e, (double)zLoop - f - direction2 + 0.5D)
                                .texture(0.0F + notExact, (float)ySum * 0.25F + minus + gaussian)
                                .color(1.0F, 1.0F, 1.0F, alpha)
                                .light(firstLight, secondLight).next();
                    }
                }
            }

            // Endgame
            if(that >= 0) {
                tessellator.draw();
            }
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            manager.disable();
        }
    }
}