package de.felixnuesse.extract.onboarding

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import ca.pkay.rcloneexplorer.R
import com.github.appintro.AppIntroBaseFragment
import com.github.appintro.AppIntroFragment
import com.github.appintro.model.SliderPage

class IdentifiableAppIntroFragment : AppIntroBaseFragment() {

    override val layoutId: Int get() = R.layout.appintro_fragment_intro

    var slideId: String = ""

    companion object {


        /**
         * Generates a new instance for [IdentifiableAppIntroFragment]
         *
         * @param title CharSequence which will be the slide title
         * @param description CharSequence which will be the slide description
         * @param imageDrawable @DrawableRes (Integer) the image that will be
         *                             displayed, obtained from Resources
         * @param backgroundColorRes @ColorRes (Integer) custom background color
         * @param titleColorRes @ColorRes (Integer) custom title color
         * @param descriptionColorRes @ColorRes (Integer) custom description color
         * @param titleTypefaceFontRes @FontRes (Integer) custom title typeface obtained
         *                             from Resources
         * @param descriptionTypefaceFontRes @FontRes (Integer) custom description typeface obtained
         *                             from Resources
         * @param backgroundDrawable @DrawableRes (Integer) custom background drawable
         *
         * @return An [AppIntroFragment] created instance
         */
        @JvmOverloads
        @JvmStatic
        fun createInstance(
            title: CharSequence? = null,
            description: CharSequence? = null,
            @DrawableRes imageDrawable: Int = 0,
            @ColorRes backgroundColorRes: Int = 0,
            @ColorRes titleColorRes: Int = 0,
            @ColorRes descriptionColorRes: Int = 0,
            @FontRes titleTypefaceFontRes: Int = 0,
            @FontRes descriptionTypefaceFontRes: Int = 0,
            @DrawableRes backgroundDrawable: Int = 0,
            id: String
        ): IdentifiableAppIntroFragment {
            return createInstance(
                SliderPage(
                    title = title,
                    description = description,
                    imageDrawable = imageDrawable,
                    backgroundColorRes = backgroundColorRes,
                    titleColorRes = titleColorRes,
                    descriptionColorRes = descriptionColorRes,
                    titleTypefaceFontRes = titleTypefaceFontRes,
                    descriptionTypefaceFontRes = descriptionTypefaceFontRes,
                    backgroundDrawable = backgroundDrawable
                ), id
            )
        }

        /**
         * Generates an [AppIntroFragment] from a given [SliderPage]
         *
         * @param sliderPage the [SliderPage] object which contains all attributes for
         * the current slide
         *
         * @return An [AppIntroFragment] created instance
         */
        @JvmStatic
        fun createInstance(sliderPage: SliderPage, id: String): IdentifiableAppIntroFragment {
            val slide = IdentifiableAppIntroFragment()
            slide.arguments = sliderPage.toBundle()
            slide.slideId = id
            return slide
        }
    }
}
