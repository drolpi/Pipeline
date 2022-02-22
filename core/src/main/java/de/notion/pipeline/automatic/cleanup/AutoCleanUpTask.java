package de.notion.pipeline.automatic.cleanup;

import de.notion.pipeline.PipelineManager;
import de.notion.pipeline.annotation.resolver.AnnotationResolver;
import de.notion.pipeline.part.local.LocalCache;
import de.notion.pipeline.registry.PipelineRegistry;

public class AutoCleanUpTask implements Runnable {

    private final PipelineManager pipelineManager;
    private final PipelineRegistry registry;
    private final LocalCache localCache;

    public AutoCleanUpTask(PipelineManager pipelineManager) {
        this.pipelineManager = pipelineManager;
        this.registry = pipelineManager.registry();
        this.localCache = pipelineManager.localCache();
    }

    @Override
    public void run() {
        registry.dataClasses()
                .stream()
                .forEach(aClass -> {
                    var optional = AnnotationResolver.cleanUp(aClass);
                    if (!optional.isPresent())
                        return;

                    var autoCleanUp = optional.get();
                    var cachedUUIDs = localCache.savedUUIDs(aClass);
                    if (cachedUUIDs.isEmpty())
                        return;
                    cachedUUIDs.forEach(uuid -> {
                        var data = localCache.data(aClass, uuid);
                        if (data == null)
                            return;
                        if ((System.currentTimeMillis() - data.lastUse()) < autoCleanUp.timeUnit().toMillis(autoCleanUp.time()))
                            return;
                        System.out.println("Cleaning up " + aClass.getSimpleName() + " with uuid " + uuid.toString());
                        data.onCleanUp();
                        pipelineManager.saveData(aClass, data.objectUUID(), () -> {

                        });
                    });
                });
    }
}
