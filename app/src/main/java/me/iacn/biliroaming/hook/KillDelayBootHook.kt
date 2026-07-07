package me.iacn.biliroaming.hook

import me.iacn.biliroaming.BiliBiliPackage.Companion.instance
import me.iacn.biliroaming.Constant
import me.iacn.biliroaming.utils.hookAfterMethod
import me.iacn.biliroaming.utils.packageName

class KillDelayBootHook(classLoader: ClassLoader) : BaseHook(classLoader) {
    override fun startHook() {
        // v9.1.0+: gripper 类从 com.bilibili.gripper.exp.a$a 改为 com.bilibili.gripper.GripperExecute
        // 方法名从 getDelayMillis 改为其他名称，需要兼容
        instance.gripperBootExpClass?.hookAfterMethod(
            if (packageName == Constant.PLAY_PACKAGE_NAME) "b" else "getDelayMillis"
        ) { param ->
            param.result = -1L
        } ?: run {
            // 回退: 尝试直接通过 GripperExecute 处理
            runCatching {
                instance.gripperBootExpClass?.declaredMethods?.firstOrNull {
                    it.returnType == Long::class.javaPrimitiveType && it.parameterCount == 0
                }?.let { method ->
                    instance.gripperBootExpClass?.hookAfterMethod(method.name) { param ->
                        param.result = -1L
                    }
                }
            }
        }
    }
}