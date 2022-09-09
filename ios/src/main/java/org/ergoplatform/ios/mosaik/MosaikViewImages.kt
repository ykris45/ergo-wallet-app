package org.ergoplatform.ios.mosaik

import org.ergoplatform.ios.ui.*
import org.ergoplatform.ios.wallet.WIDTH_ICONS
import org.ergoplatform.mosaik.TreeElement
import org.ergoplatform.mosaik.model.ui.ForegroundColor
import org.ergoplatform.mosaik.model.ui.Icon
import org.ergoplatform.mosaik.model.ui.IconType
import org.ergoplatform.mosaik.model.ui.Image
import org.robovm.apple.foundation.NSData
import org.robovm.apple.uikit.*

class ImageViewHolder(
    treeElement: TreeElement
): UiViewHolder(UIImageView(), treeElement) {

    val uiImageView = uiView as UIImageView

    init {
        val mosaikViewElement = treeElement.element as Image
        val size = when (mosaikViewElement.size) {
            Image.Size.SMALL -> 50.0
            Image.Size.MEDIUM -> 120.0
            Image.Size.LARGE -> 250.0
        }
        uiImageView.apply {
            contentMode = UIViewContentMode.ScaleAspectFit
            fixedHeight(size)
            fixedWidth(size)
        }

        treeElement.getResourceBytes?.let { resourceBytesAvailable(it) }
    }

    override fun resourceBytesAvailable(bytes: ByteArray) {
        super.resourceBytesAvailable(bytes)
        // if we have an image that is not set yet
        if (uiImageView.image == null) {
            try {
                uiImageView.image = UIImage(NSData(bytes))
            } catch (t: Throwable) {

            }
        }
    }
}

class IconImageViewHolder(
    treeElement: TreeElement
): UiViewHolder(UIImageView(), treeElement) {

    val uiImageView = uiView as UIImageView

    init {
        val mosaikViewElement = treeElement.element as Icon
        val size = when (mosaikViewElement.iconSize) {
            Icon.Size.SMALL -> WIDTH_ICONS
            Icon.Size.MEDIUM -> WIDTH_ICONS * 2
            Icon.Size.LARGE -> WIDTH_ICONS * 4
        }
        uiImageView.apply {
            contentMode = UIViewContentMode.ScaleAspectFit
            fixedHeight(size)
            fixedWidth(size)
            tintColor = when (mosaikViewElement.tintColor) {
                ForegroundColor.PRIMARY -> uiColorErgo
                ForegroundColor.DEFAULT -> UIColor.label()
                ForegroundColor.SECONDARY -> UIColor.secondaryLabel()
            }

            image = getIosSystemImage(
                when (mosaikViewElement.iconType) {
                    IconType.INFO -> IMAGE_INFORMATION
                    IconType.WARN -> IMAGE_WARNING
                    IconType.ERROR -> IMAGE_ERROR
                    IconType.CONFIG -> IMAGE_SETTINGS
                    IconType.ADD -> IMAGE_PLUS
                    IconType.EDIT -> IMAGE_EDIT_CIRCLE
                    IconType.REFRESH -> IMAGE_RELOAD
                    IconType.DELETE -> "xmark.circle"
                    IconType.CROSS -> "xmark"
                    IconType.WALLET -> IMAGE_WALLET
                    IconType.SEND -> IMAGE_SEND
                    IconType.RECEIVE -> IMAGE_RECEIVE
                    IconType.MORE -> IMAGE_MORE_ACTION
                    IconType.OPENLIST -> IMAGE_OPEN_LIST
                    IconType.CHEVRON_UP -> IMAGE_CHEVRON_UP
                    IconType.CHEVRON_DOWN -> IMAGE_CHEVRON_DOWN
                    IconType.COPY -> "doc.on.doc"
                    IconType.BACK -> IMAGE_CHEVRON_LEFT
                    IconType.FORWARD -> "chevron.right"
                    IconType.SWITCH -> "arrow.left.and.right.circle"
                    IconType.QR_CODE -> IMAGE_QR_CODE
                    IconType.QR_SCAN -> IMAGE_QR_SCAN
                },
                UIImageSymbolScale.Small
            )
        }
    }
}