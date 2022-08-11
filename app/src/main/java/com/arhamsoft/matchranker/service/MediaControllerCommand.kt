package com.arhamsoft.matchranker.service

/**
 * Copyright (C) 2017 Apple, Inc. All rights reserved.
 */
interface MediaControllerCommand {
    companion object {
        const val COMMAND_REMOVE_QUEUE_ITEM =
            "com.apple.android.music.playback.command.REMOVE_QUEUE_ITEM"
        const val COMMAND_MOVE_QUEUE_ITEM =
            "com.apple.android.music.playback.command.MOVE_QUEUE_ITEM"
        const val COMMAND_ADD_QUEUE_ITEMS =
            "com.apple.android.music.playback.command.ADD_QUEUE_ITEMS"
        const val COMMAND_ARGUMENT_PLAYBACK_QUEUE_ID =
            "com.apple.android.music.playback.command.ARGUMENT_PLAYBACK_QUEUE_ID"
        const val COMMAND_ARGUMENT_PLAYBACK_QUEUE_ID_TARGET =
            "com.apple.android.music.playback.command.ARGUMENT_PLAYBACK_QUEUE_ID_TARGET"
        const val COMMAND_ARGUMENT_PLAYBACK_QUEUE_MOVE_TARGET_TYPE =
            "com.apple.android.music.playback.command.ARGUMENT_PLAYBACK_QUEUE_MOVE_TARGET_TYPE"
        const val COMMAND_ARGUMENT_PLAYBACK_QUEUE_INSERTION_TYPE =
            "com.apple.android.music.playback.command.ARGUMENT_PLAYBACK_QUEUE_INSERTION_TYPE"
        const val COMMAND_ARGUMENT_PLAYBACK_QUEUE_ITEM_PROVIDER =
            "com.apple.android.music.playback.command.ARGUMENT_PLAYBACK_QUEUE_ITEM_PROVIDER"
    }
}