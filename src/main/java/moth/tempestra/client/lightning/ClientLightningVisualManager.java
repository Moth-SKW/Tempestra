package moth.tempestra.client.lightning;

import moth.butterflyapi.math.Basis3;
import moth.butterflyapi.math.Vecs;
//? >=1.21.11 {
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
//?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
*///?}
import net.minecraft.client.MinecraftClient;
//? >=1.21.11 {
import net.minecraft.client.render.RenderLayers;
//?} else {
/*import net.minecraft.client.render.RenderLayer;
*///?}
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LightningEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ClientLightningVisualManager {
    private static final Map<Integer, LightningBoltVisualData> ACTIVE_BOLTS = new LinkedHashMap<>();

    private ClientLightningVisualManager() {
    }

    public static void ensureVisual(LightningEntity entity) {
        if (!TempestraLightningVisuals.CUSTOM_VISUALS_ENABLED || !(entity.getEntityWorld() instanceof ClientWorld world)) {
            return;
        }

        LightningBoltVisualData data = ACTIVE_BOLTS.get(entity.getId());
        if (data == null) {
            data = LightningShapeGenerator.generate(entity, world);
            ACTIVE_BOLTS.put(entity.getId(), data);
            ClientLightningImpactEffects.start(world, data);
        }
        data.markSeen(world.getTime());
    }

    public static void tick(MinecraftClient client) {
        if (client.world == null) {
            clear();
            return;
        }

        Iterator<LightningBoltVisualData> iterator = ACTIVE_BOLTS.values().iterator();
        while (iterator.hasNext()) {
            LightningBoltVisualData data = iterator.next();
            data.tick();
            ClientLightningImpactEffects.tick(client.world, data);
            if (data.isExpired()) {
                iterator.remove();
            }
        }
    }

    public static void render(WorldRenderContext context) {
        //? >=1.21.11 {
        if (ACTIVE_BOLTS.isEmpty() || context.matrices() == null || context.consumers() == null
                || context.worldState().cameraRenderState == null) {
            return;
        }

        MatrixStack matrices = context.matrices();
        Vec3d cameraPos = context.worldState().cameraRenderState.pos;
        float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false);
        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        VertexConsumer consumer = context.consumers().getBuffer(RenderLayers.lightning());
        for (LightningBoltVisualData data : ACTIVE_BOLTS.values()) {
            renderBolt(positionMatrix, consumer, data, tickDelta, cameraPos);
            renderImpact(positionMatrix, consumer, data, tickDelta);
            renderGroundImpactParticles(positionMatrix, consumer, data, tickDelta);
            renderSparks(positionMatrix, consumer, data, tickDelta);
        }

        matrices.pop();
        //?} else {
        /*if (ACTIVE_BOLTS.isEmpty() || context.matrixStack() == null || context.consumers() == null || context.camera() == null) {
            return;
        }

        MatrixStack matrices = context.matrixStack();
        Vec3d cameraPos = context.camera().getPos();
        float tickDelta = renderTickDelta(context);
        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        VertexConsumer consumer = context.consumers().getBuffer(RenderLayer.getLightning());
        for (LightningBoltVisualData data : ACTIVE_BOLTS.values()) {
            renderBolt(positionMatrix, consumer, data, tickDelta, cameraPos);
            renderImpact(positionMatrix, consumer, data, tickDelta);
            renderGroundImpactParticles(positionMatrix, consumer, data, tickDelta);
            renderSparks(positionMatrix, consumer, data, tickDelta);
        }

        matrices.pop();
        *///?}
    }

    private static float renderTickDelta(WorldRenderContext context) {
        //? >=1.21.11 {
        return MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false);
        //?} elif >=1.21.1 {
        /*return context.tickCounter().getTickDelta(false);
        *///?} else {
        /*return context.tickDelta();
        *///?}
    }

    public static void clear() {
        ACTIVE_BOLTS.clear();
    }

    private static void renderBolt(Matrix4f positionMatrix, VertexConsumer consumer, LightningBoltVisualData data, float tickDelta, Vec3d cameraPos) {
        float lifetimeFade = 1.0F - smoothstep(0.58F, 1.0F, data.lifetimeProgress(tickDelta));
        float strikeFlash = 0.72F + flicker(data.seed(), data.renderAge(tickDelta), 0.0F) * 0.34F;
        for (LightningPath path : data.paths()) {
            renderBlendedPath(positionMatrix, consumer, path, data, tickDelta, cameraPos, lifetimeFade, strikeFlash);
        }
    }

    private static void renderBlendedPath(
            Matrix4f positionMatrix,
            VertexConsumer consumer,
            LightningPath path,
            LightningBoltVisualData data,
            float tickDelta,
            Vec3d cameraPos,
            float lifetimeFade,
            float strikeFlash
    ) {
        if (path.segments().isEmpty()) {
            return;
        }

        PathSegmentVisual[] visuals = new PathSegmentVisual[path.segments().size()];
        for (int index = 0; index < path.segments().size(); index++) {
            LightningSegment segment = path.segments().get(index);
            float segmentFlicker = segmentFlicker(path.type(), segment, data.renderAge(tickDelta));
            float baseAlphaScale = path.alphaScale() * segment.energy() * segmentFlicker * lifetimeFade * strikeFlash;
            float startAlphaScale = baseAlphaScale * pathFade(path.type(), segment.localStartProgress(), segment.startProgress());
            float endAlphaScale = baseAlphaScale * pathFade(path.type(), segment.localEndProgress(), segment.endProgress());
            float distanceReadability = distanceReadability(cameraPos, segment);
            if (path.type() != LightningPathType.MAIN) {
                float readabilityBoost = 1.0F + distanceReadability * TempestraLightningVisuals.DISTANT_SECONDARY_ALPHA_BOOST;
                startAlphaScale *= readabilityBoost;
                endAlphaScale *= readabilityBoost;
            }

            float widthJitter = 0.84F + flicker(segment.flickerSeed() ^ 0x7A1D2E6B62D19F5AL, data.renderAge(tickDelta), 1.0F) * 0.28F;
            float radiusBoost = 1.0F + distanceReadability * TempestraLightningVisuals.DISTANT_RADIUS_BOOST;
            float widthScale = path.widthScale() * segment.widthScale() * widthJitter * (0.8F + data.intensity() * 0.22F) * radiusBoost;
            visuals[index] = new PathSegmentVisual(
                    widthScale,
                    startAlphaScale,
                    endAlphaScale,
                    pathRadiusScale(path.type(), segment.localStartProgress(), segment.startProgress()),
                    pathRadiusScale(path.type(), segment.localEndProgress(), segment.endProgress()),
                    path.type() == LightningPathType.MAIN || segmentFlicker > 0.46F
            );
        }

        float flashBloom = path.type() == LightningPathType.MAIN
                ? 1.0F - smoothstep(0.0F, 3.8F, data.renderAge(tickDelta))
                : 0.0F;
        if (flashBloom > 0.01F) {
            renderBlendedPathPass(positionMatrix, consumer, path.segments(), visuals,
                    TempestraLightningVisuals.MAIN_OUTER_RADIUS, colorForOuter(path.type()), 38.0F * flashBloom, 1.75F, 1.48F, false);
        }
        renderBlendedPathPass(positionMatrix, consumer, path.segments(), visuals,
                TempestraLightningVisuals.MAIN_OUTER_RADIUS, colorForOuter(path.type()), 108.0F, 1.0F, 1.0F, false);
        renderBlendedPathPass(positionMatrix, consumer, path.segments(), visuals,
                TempestraLightningVisuals.MAIN_INNER_RADIUS, TempestraLightningVisuals.INNER_COLOR, 190.0F, 1.0F, 1.0F, false);
        renderBlendedPathPass(positionMatrix, consumer, path.segments(), visuals,
                TempestraLightningVisuals.MAIN_CORE_RADIUS, TempestraLightningVisuals.CORE_COLOR, 245.0F, 1.0F, 1.0F, true);
    }

    private static void renderBlendedPathPass(
            Matrix4f positionMatrix,
            VertexConsumer consumer,
            java.util.List<LightningSegment> segments,
            PathSegmentVisual[] visuals,
            float baseRadius,
            int color,
            float alpha,
            float startRadiusMultiplier,
            float endRadiusMultiplier,
            boolean corePass
    ) {
        PathVertex[] vertices = new PathVertex[segments.size() + 1];
        float maximumAlpha = 0.0F;
        for (int index = 0; index < vertices.length; index++) {
            float pathMultiplier = MathHelper.lerp(index / (float) segments.size(), startRadiusMultiplier, endRadiusMultiplier);
            float radius;
            float vertexAlpha;
            Vec3d position;
            if (index == 0) {
                PathSegmentVisual visual = visuals[0];
                radius = radiusFor(visual, baseRadius, visual.startRadiusScale(), pathMultiplier);
                vertexAlpha = alphaFor(visual.startAlphaScale(), alpha, corePass && !visual.coreVisible());
                position = segments.get(0).start();
            } else if (index == segments.size()) {
                PathSegmentVisual visual = visuals[index - 1];
                radius = radiusFor(visual, baseRadius, visual.endRadiusScale(), pathMultiplier);
                vertexAlpha = alphaFor(visual.endAlphaScale(), alpha, corePass && !visual.coreVisible());
                position = segments.get(index - 1).end();
            } else {
                PathSegmentVisual previous = visuals[index - 1];
                PathSegmentVisual next = visuals[index];
                radius = (radiusFor(previous, baseRadius, previous.endRadiusScale(), pathMultiplier)
                        + radiusFor(next, baseRadius, next.startRadiusScale(), pathMultiplier)) * 0.5F;
                vertexAlpha = (alphaFor(previous.endAlphaScale(), alpha, corePass && !previous.coreVisible())
                        + alphaFor(next.startAlphaScale(), alpha, corePass && !next.coreVisible())) * 0.5F;
                position = segments.get(index - 1).end();
            }
            maximumAlpha = Math.max(maximumAlpha, vertexAlpha);
            vertices[index] = new PathVertex(position, radius, Math.round(vertexAlpha), null, null);
        }
        if (maximumAlpha <= 0.5F) {
            return;
        }

        Vec3d previousNormal = null;
        for (int index = 0; index < vertices.length; index++) {
            Vec3d tangent = pathTangent(vertices, index);
            Vec3d normal = transportNormal(tangent, previousNormal);
            Vec3d binormal = Vecs.safeNormalize(tangent.crossProduct(normal), stableNormal(tangent));
            PathVertex vertex = vertices[index];
            vertices[index] = new PathVertex(vertex.position(), vertex.radius(), vertex.alpha(), normal, binormal);
            previousNormal = normal;
        }
        emitBlendedTube(positionMatrix, consumer, vertices, red(color), green(color), blue(color));
    }

    private static float radiusFor(PathSegmentVisual visual, float baseRadius, float radiusScale, float pathMultiplier) {
        return Math.max(0.0F, baseRadius * visual.widthScale() * radiusScale * pathMultiplier);
    }

    private static float alphaFor(float alphaScale, float passAlpha, boolean hidden) {
        return hidden ? 0.0F : MathHelper.clamp(alphaScale * passAlpha, 0.0F, 255.0F);
    }

    private static Vec3d pathTangent(PathVertex[] vertices, int index) {
        Vec3d forward = index == vertices.length - 1
                ? vertices[index].position().subtract(vertices[index - 1].position())
                : vertices[Math.min(index + 1, vertices.length - 1)].position().subtract(vertices[Math.max(index - 1, 0)].position());
        return Vecs.safeNormalize(forward, new Vec3d(0.0D, -1.0D, 0.0D));
    }

    private static Vec3d transportNormal(Vec3d tangent, Vec3d previousNormal) {
        if (previousNormal == null) {
            return stableNormal(tangent);
        }
        Vec3d projected = previousNormal.subtract(tangent.multiply(previousNormal.dotProduct(tangent)));
        return Vecs.safeNormalize(projected, stableNormal(tangent));
    }

    private static Vec3d stableNormal(Vec3d tangent) {
        Vec3d reference = Math.abs(tangent.y) < 0.92D
                ? new Vec3d(0.0D, 1.0D, 0.0D)
                : new Vec3d(1.0D, 0.0D, 0.0D);
        return Vecs.safeNormalize(tangent.crossProduct(reference), new Vec3d(0.0D, 0.0D, 1.0D));
    }

    private static void emitBlendedTube(Matrix4f positionMatrix, VertexConsumer consumer, PathVertex[] vertices, int red, int green, int blue) {
        for (int index = 0; index < vertices.length - 1; index++) {
            PathVertex start = vertices[index];
            PathVertex end = vertices[index + 1];
            if ((start.alpha() <= 0 && end.alpha() <= 0) || (start.radius() <= 0.0F && end.radius() <= 0.0F)) {
                continue;
            }
            for (int side = 0; side < TempestraLightningVisuals.TUBE_SIDES; side++) {
                double startAngle = (Math.PI * 2.0D * side) / TempestraLightningVisuals.TUBE_SIDES;
                double endAngle = (Math.PI * 2.0D * (side + 1)) / TempestraLightningVisuals.TUBE_SIDES;
                Vec3d startA = start.position().add(ringOffset(start, startAngle));
                Vec3d startB = start.position().add(ringOffset(start, endAngle));
                Vec3d endA = end.position().add(ringOffset(end, startAngle));
                Vec3d endB = end.position().add(ringOffset(end, endAngle));
                emitQuad(positionMatrix, consumer, startA, startB, endB, endA, red, green, blue, start.alpha(), end.alpha());
                emitQuad(positionMatrix, consumer, startB, startA, endA, endB, red, green, blue, start.alpha(), end.alpha());
            }
        }
    }

    private static Vec3d ringOffset(PathVertex vertex, double angle) {
        return vertex.normal().multiply(Math.cos(angle) * vertex.radius())
                .add(vertex.binormal().multiply(Math.sin(angle) * vertex.radius()));
    }

    private static void renderImpact(Matrix4f positionMatrix, VertexConsumer consumer, LightningBoltVisualData data, float tickDelta) {
        float age = data.renderAge(tickDelta);
        float progress = Math.min(1.0F, age / 6.0F);
        boolean copperImpact = data.impactType() == LightningImpactType.COPPER;
        float alphaScale = (1.0F - smoothstep(0.18F, 1.0F, progress))
                * (0.65F + flicker(data.seed() ^ 0x432AF7D84C2B6B0DL, age, 2.0F) * 0.35F)
                * (copperImpact ? 1.34F : 1.0F);
        if (alphaScale <= 0.01F) {
            return;
        }

        for (LightningImpactRay ray : data.impactRays()) {
            Vec3d contact = ray.contact().add(0.0D, 0.08D, 0.0D);
            float rayFlicker = 0.52F + flicker(ray.flickerSeed(), age, 3.0F) * 0.58F;
            Vec3d end = contact.add(ray.offset().multiply(0.55D + progress * 0.55D));
            LightningSegment segment = new LightningSegment(contact, end, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, ray.flickerSeed());
            renderSegmentPass(
                    positionMatrix,
                    consumer,
                    segment,
                    TempestraLightningVisuals.MAIN_INNER_RADIUS * ray.widthScale() * (1.0F - progress * 0.45F),
                    TempestraLightningVisuals.MAIN_INNER_RADIUS * ray.widthScale() * (0.22F + progress * 0.08F),
                    copperImpact ? TempestraLightningVisuals.COPPER_IMPACT_COLOR : TempestraLightningVisuals.IMPACT_COLOR,
                    Math.round(188.0F * alphaScale * rayFlicker),
                    Math.round(36.0F * alphaScale * rayFlicker)
            );
            renderSegmentPass(
                    positionMatrix,
                    consumer,
                    segment,
                    TempestraLightningVisuals.MAIN_OUTER_RADIUS * (copperImpact ? 0.62F : 0.42F) * ray.widthScale(),
                    TempestraLightningVisuals.MAIN_OUTER_RADIUS * 0.08F * ray.widthScale(),
                    copperImpact ? TempestraLightningVisuals.COPPER_ELECTRIC_COLOR : TempestraLightningVisuals.OUTER_COLOR,
                    Math.round(76.0F * alphaScale * rayFlicker),
                    Math.round(18.0F * alphaScale * rayFlicker)
            );
        }
    }

    private static void renderGroundImpactParticles(Matrix4f positionMatrix, VertexConsumer consumer, LightningBoltVisualData data, float tickDelta) {
        for (LightningGroundImpactParticle particle : data.groundImpactParticles()) {
            float progress = particle.ageProgress(tickDelta);
            float alphaScale = 1.0F - smoothstep(0.34F, 1.0F, progress);
            if (alphaScale <= 0.01F) {
                continue;
            }
            Vec3d current = particle.interpolatedPosition(tickDelta);
            Vec3d tail = current.subtract(particle.velocity().multiply(2.4D));
            LightningSegment streak = new LightningSegment(tail, current, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, data.seed());
            float radius = particle.radius() * (1.0F - progress * 0.45F);
            renderSegmentPass(positionMatrix, consumer, streak, radius * 0.35F, radius, particle.color(), Math.round(62.0F * alphaScale), Math.round(194.0F * alphaScale));
            renderSegmentPass(positionMatrix, consumer, streak, radius * 0.75F, radius * 1.85F, TempestraLightningVisuals.OUTER_COLOR, Math.round(16.0F * alphaScale), Math.round(52.0F * alphaScale));
        }
    }

    private static void renderSparks(Matrix4f positionMatrix, VertexConsumer consumer, LightningBoltVisualData data, float tickDelta) {
        for (LightningSpark spark : data.sparks()) {
            float progress = spark.ageProgress(tickDelta);
            float alphaScale = 1.0F - smoothstep(0.25F, 1.0F, progress);
            if (alphaScale <= 0.01F) {
                continue;
            }
            Vec3d current = spark.interpolatedPosition(tickDelta);
            Vec3d tail = current.subtract(spark.velocity().multiply(1.85D));
            LightningSegment segment = new LightningSegment(tail, current, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, data.seed());
            float headRadius = spark.radius() * (1.0F - progress * 0.55F);
            renderSegmentPass(positionMatrix, consumer, segment, headRadius * 0.42F, headRadius, spark.color(), Math.round(74.0F * alphaScale), Math.round(218.0F * alphaScale));
            renderSegmentPass(positionMatrix, consumer, segment, spark.radius() * 0.82F, spark.radius() * 2.2F, TempestraLightningVisuals.OUTER_COLOR, Math.round(18.0F * alphaScale), Math.round(58.0F * alphaScale));
        }
    }

    private static void renderSegmentPass(Matrix4f positionMatrix, VertexConsumer consumer, LightningSegment segment, float radius, int color, int alpha) {
        renderSegmentPass(positionMatrix, consumer, segment, radius, color, alpha, alpha);
    }

    private static void renderSegmentPass(Matrix4f positionMatrix, VertexConsumer consumer, LightningSegment segment, float radius, int color, int startAlpha, int endAlpha) {
        renderSegmentPass(positionMatrix, consumer, segment, radius, radius * 0.84F, color, startAlpha, endAlpha);
    }

    private static void renderSegmentPass(Matrix4f positionMatrix, VertexConsumer consumer, LightningSegment segment, float startRadius, float endRadius, int color, int startAlpha, int endAlpha) {
        if ((startAlpha <= 0 && endAlpha <= 0) || (startRadius <= 0.0F && endRadius <= 0.0F)) {
            return;
        }
        boolean gradientAlpha = startAlpha != endAlpha;
        emitTubeSegment(
                positionMatrix,
                consumer,
                segment.start(),
                segment.end(),
                Math.max(0.0F, startRadius),
                Math.max(0.0F, endRadius),
                red(color),
                green(color),
                blue(color),
                MathHelper.clamp(startAlpha, 0, 255),
                MathHelper.clamp(gradientAlpha ? endAlpha : Math.round(endAlpha * 0.88F), 0, 255),
                TempestraLightningVisuals.TUBE_SIDES
        );
    }

    private static void emitTubeSegment(
            Matrix4f positionMatrix,
            VertexConsumer consumer,
            Vec3d start,
            Vec3d end,
            float startRadius,
            float endRadius,
            int red,
            int green,
            int blue,
            int startAlpha,
            int endAlpha,
            int sides
    ) {
        Vec3d axis = end.subtract(start);
        if (Vecs.lengthSquared(axis) < 1.0E-6D) {
            return;
        }

        Basis3 basis = Basis3.fromForward(axis);
        for (int side = 0; side < sides; side++) {
            double startAngle = (Math.PI * 2.0D * side) / sides;
            double endAngle = (Math.PI * 2.0D * (side + 1)) / sides;
            Vec3d startOffsetA = basis.radial(startAngle, startRadius);
            Vec3d startOffsetB = basis.radial(endAngle, startRadius);
            Vec3d endOffsetA = basis.radial(startAngle, endRadius);
            Vec3d endOffsetB = basis.radial(endAngle, endRadius);
            emitQuad(
                    positionMatrix,
                    consumer,
                    start.add(startOffsetA),
                    start.add(startOffsetB),
                    end.add(endOffsetB),
                    end.add(endOffsetA),
                    red,
                    green,
                    blue,
                    startAlpha,
                    endAlpha
            );
            emitQuad(
                    positionMatrix,
                    consumer,
                    start.add(startOffsetB),
                    start.add(startOffsetA),
                    end.add(endOffsetA),
                    end.add(endOffsetB),
                    red,
                    green,
                    blue,
                    startAlpha,
                    endAlpha
            );
        }
    }

    private static void emitQuad(
            Matrix4f positionMatrix,
            VertexConsumer consumer,
            Vec3d first,
            Vec3d second,
            Vec3d third,
            Vec3d fourth,
            int red,
            int green,
            int blue,
            int startAlpha,
            int endAlpha
    ) {
        consumer.vertex(positionMatrix, (float) first.x, (float) first.y, (float) first.z).color(red, green, blue, startAlpha);
        //? <1.21 {
        /*consumer.next();
        *///?}
        consumer.vertex(positionMatrix, (float) second.x, (float) second.y, (float) second.z).color(red, green, blue, startAlpha);
        //? <1.21 {
        /*consumer.next();
        *///?}
        consumer.vertex(positionMatrix, (float) third.x, (float) third.y, (float) third.z).color(red, green, blue, endAlpha);
        //? <1.21 {
        /*consumer.next();
        *///?}
        consumer.vertex(positionMatrix, (float) fourth.x, (float) fourth.y, (float) fourth.z).color(red, green, blue, endAlpha);
        //? <1.21 {
        /*consumer.next();
        *///?}
    }

    private static float segmentFlicker(LightningPathType type, LightningSegment segment, float age) {
        float sample = flicker(segment.flickerSeed(), age, segment.endProgress() * 6.0F);
        return switch (type) {
            case MAIN -> 0.72F + sample * 0.34F;
            case SPLIT -> sample < 0.08F ? 0.0F : 0.42F + sample * 0.66F;
            case BRANCH -> sample < 0.18F ? 0.0F : 0.34F + sample * 0.70F;
            case FILAMENT -> sample < 0.26F ? 0.0F : 0.24F + sample * 0.62F;
        };
    }

    private static float secondaryFade(LightningPathType type, float localEndProgress) {
        return Math.max(secondaryFadeFloor(type), 1.0F - smoothstep(0.58F, 1.0F, localEndProgress));
    }

    private static float pathFade(LightningPathType type, float localProgress, float globalProgress) {
        if (type == LightningPathType.MAIN) {
            return 1.0F;
        }

        float secondaryFade = secondaryFade(type, localProgress);
        float contactFade = Math.max(secondaryFadeFloor(type) * 0.72F, 1.0F - smoothstep(0.74F, 0.96F, globalProgress));
        return secondaryFade * contactFade;
    }

    private static float pathRadiusScale(LightningPathType type, float localProgress, float globalProgress) {
        return switch (type) {
            case MAIN -> Math.max(
                    0.78F,
                    (0.86F + smoothstep(0.0F, 0.14F, globalProgress) * 0.14F)
                            * (1.0F - smoothstep(0.84F, 1.0F, globalProgress) * 0.12F)
            );
            case SPLIT -> Math.max(0.48F, 1.0F - smoothstep(0.34F, 1.0F, localProgress) * 0.46F);
            case BRANCH -> Math.max(0.20F, 1.0F - smoothstep(0.16F, 1.0F, localProgress) * 0.80F);
            case FILAMENT -> Math.max(0.10F, 0.76F - smoothstep(0.0F, 1.0F, localProgress) * 0.66F);
        };
    }

    private static float secondaryFadeFloor(LightningPathType type) {
        return switch (type) {
            case MAIN -> 1.0F;
            case SPLIT -> TempestraLightningVisuals.SPLIT_FADE_ALPHA_FLOOR;
            case BRANCH -> TempestraLightningVisuals.BRANCH_FADE_ALPHA_FLOOR;
            case FILAMENT -> 0.08F;
        };
    }

    private static float distanceReadability(Vec3d cameraPos, LightningSegment segment) {
        Vec3d midpoint = segment.start().add(segment.end()).multiply(0.5D);
        float distance = (float) cameraPos.distanceTo(midpoint);
        return smoothstep(
                TempestraLightningVisuals.DISTANT_READABILITY_START,
                TempestraLightningVisuals.DISTANT_READABILITY_END,
                distance
        );
    }

    private static float flicker(long seed, float age, float salt) {
        long phase = (long) Math.floor((age + salt) * 3.0F);
        long mixed = mix(seed ^ (phase * 0x9E3779B97F4A7C15L) ^ ((long) (salt * 1000.0F) * 0xD1B54A32D192ED03L));
        return ((mixed >>> 40) & 0xFFFFFF) / (float) 0xFFFFFF;
    }

    private static long mix(long value) {
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return value;
    }

    private static int colorForOuter(LightningPathType type) {
        return switch (type) {
            case MAIN -> TempestraLightningVisuals.OUTER_COLOR;
            case SPLIT, FILAMENT -> TempestraLightningVisuals.EDGE_COLOR;
            case BRANCH -> TempestraLightningVisuals.OUTER_COLOR;
        };
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float t = MathHelper.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    private static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    private static int blue(int color) {
        return color & 0xFF;
    }

    private record PathSegmentVisual(
            float widthScale,
            float startAlphaScale,
            float endAlphaScale,
            float startRadiusScale,
            float endRadiusScale,
            boolean coreVisible
    ) {
    }

    private record PathVertex(Vec3d position, float radius, int alpha, Vec3d normal, Vec3d binormal) {
    }
}
