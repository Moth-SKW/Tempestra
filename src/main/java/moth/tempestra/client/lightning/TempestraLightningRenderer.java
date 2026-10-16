package moth.tempestra.client.lightning;

import moth.tempestra.TempestraMod;
//? >=1.21.11 {
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
//?} else {
/*import net.minecraft.client.render.VertexConsumerProvider;
*///?}
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
//? >=1.21.11 {
import net.minecraft.client.render.entity.state.LightningEntityRenderState;
//?}
import net.minecraft.client.render.Frustum;
//? >=1.21.11 {
import net.minecraft.client.render.state.CameraRenderState;
//?}
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LightningEntity;

//? >=1.21.11 {
public final class TempestraLightningRenderer extends EntityRenderer<LightningEntity, LightningEntityRenderState> {
    public TempestraLightningRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public LightningEntityRenderState createRenderState() {
        return new LightningEntityRenderState();
    }

    @Override
    public boolean shouldRender(LightningEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void updateRenderState(LightningEntity entity, LightningEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        ClientLightningVisualManager.ensureVisual(entity);
    }

    @Override
    public void render(
            LightningEntityRenderState state,
            MatrixStack matrices,
            OrderedRenderCommandQueue commandQueue,
            CameraRenderState cameraState
    ) {
    }

}
//?} else {
/*public final class TempestraLightningRenderer extends EntityRenderer<LightningEntity> {
    public TempestraLightningRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(LightningEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(
            LightningEntity entity,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light
    ) {
        ClientLightningVisualManager.ensureVisual(entity);
    }

    @Override
    public net.minecraft.util.Identifier getTexture(LightningEntity entity) {
        return TempestraMod.id("textures/misc/lightning_placeholder.png");
    }
}
*///?}
