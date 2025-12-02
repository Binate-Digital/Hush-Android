package com.fictivestudios.hush.base.fragment

import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseFragment(layout: Int) : Fragment(layout) {

    protected abstract fun initialize()

    protected abstract fun setObserver()

    protected abstract fun setOnClickListener()

    fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    fun View.hide() {
        this.visibility = View.INVISIBLE
    }

    fun disableUserTouch(){
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
    fun enableUserTouch(){
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    fun View.show() {
        this.visibility = View.VISIBLE
    }

    fun View.showProgress() {
        this.visibility = View.VISIBLE
    }

    fun View.gone() {
        this.visibility = View.GONE
    }

}