package com.fictivestudios.hush.utils.customLoader

import android.content.Context
import android.util.AttributeSet
import com.fictivestudios.hush.R

/**
 * Created by Hammad Sheikh on 11/04/24.
 */
abstract class ThreeDotsBaseView : AbstractLinearLayout {

    var firstDotColor: Int = resources.getColor(R.color.theme_red)

    var secondDotColor: Int = resources.getColor(R.color.theme_red)

    var thirdDotColor: Int = resources.getColor(R.color.theme_red)

    protected lateinit var firstCircle: CircleView
    protected lateinit var secondCircle: CircleView
    protected lateinit var thirdCircle: CircleView

    constructor(context: Context, dotsRadius: Int, dotsDist: Int,
                firstDotColor: Int, secondDotColor: Int, thirdDotColor: Int) : super(context) {
        this.dotsRadius = dotsRadius
        this.dotsDist = dotsDist
        this.firstDotColor = firstDotColor
        this.secondDotColor = secondDotColor
        this.thirdDotColor = thirdDotColor
    }

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
}