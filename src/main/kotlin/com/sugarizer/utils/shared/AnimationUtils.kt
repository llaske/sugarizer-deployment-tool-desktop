package com.sugarizer.utils.shared

import javafx.animation.FadeTransition
import javafx.scene.Node
import javafx.util.Duration

class AnimationUtils {
    fun fadeIn(node: Node): FadeTransition {
        var fadeIn = FadeTransition(Duration.millis(250.0), node)

        fadeIn.fromValue = 0.0
        fadeIn.toValue = 1.0

        return fadeIn
    }

    fun fadeOut(node: Node): FadeTransition {
        var fadeOut = FadeTransition(Duration.millis(250.0), node)

        fadeOut.fromValue = 1.0
        fadeOut.toValue = 0.0
        fadeOut.setOnFinished { node.isVisible = false }

        return fadeOut
    }
}