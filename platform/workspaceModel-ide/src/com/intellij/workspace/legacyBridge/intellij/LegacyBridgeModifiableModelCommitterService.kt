package com.intellij.workspace.legacyBridge.intellij

import com.intellij.openapi.module.ModifiableModuleModel
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.impl.ModifiableModelCommitterService
import com.intellij.workspace.api.TypedEntityStorageBuilder
import com.intellij.workspace.ide.WorkspaceModel
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
internal class LegacyBridgeModifiableModelCommitterService : ModifiableModelCommitterService {
  override fun multiCommit(rootModels: MutableCollection<out ModifiableRootModel>, moduleModel: ModifiableModuleModel) {
    // TODO Naive impl, check for existing contact in com.intellij.openapi.module.impl.ModuleManagerImpl.commitModelWithRunnable
    val diffs = mutableSetOf<TypedEntityStorageBuilder>()
    for (rootModel in rootModels) {
      if (rootModel.isChanged) {
        diffs += (rootModel as LegacyBridgeModifiableRootModel).collectChanges() ?: continue
      }
      else rootModel.dispose()
    }
    diffs += (moduleModel as LegacyBridgeModifiableModuleModel).collectChanges()

    WorkspaceModel.getInstance(moduleModel.project).updateProjectModel {
      diffs.forEach(it::addDiff)
    }

    rootModels.forEach { it.dispose() }
  }
}