package de.notion.pipeline.automatic.cleanup;

import de.notion.pipeline.PipelineManager;
import de.notion.pipeline.annotation.resolver.AnnotationResolver;
import de.notion.pipeline.config.PipelineRegistry;
import de.notion.pipeline.part.local.LocalCache;

public final class CleanUpTask implements Runnable {

    private final PipelineManager pipelineManager;
    private final PipelineRegistry registry;
    private final LocalCache localCache;

    public CleanUpTask(PipelineManager pipelineManager) {
        this.pipelineManager = pipelineManager;
        this.registry = pipelineManager.registry();
        this.localCache = pipelineManager.localCache();
    }

    @Override
    public void run() {
        registry.dataClasses()
                .stream()
                .forEach(dataClass -> {
                    var optional = AnnotationResolver.cleanUp(dataClass);

                    optional.ifPresent(cleanUp -> {
                        var cachedUUIDs = localCache.savedUUIDs(dataClass);

                        cachedUUIDs.forEach(uuid -> {
                            var data = localCache.data(dataClass, uuid);
                            if (data == null)
                                return;
                            if ((System.currentTimeMillis() - data.lastUse()) < cleanUp.timeUnit().toMillis(cleanUp.time()))
                                return;

                            System.out.println("Cleaning up " + dataClass.getSimpleName() + " with uuid " + uuid.toString());
                            data.onCleanUp();
                            pipelineManager.cleanUpData(dataClass, data.objectUUID(), null);
                        });
                    });
                });
    }
}
