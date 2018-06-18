package com.tompee.utilities.filldevicespace.feature.main.easyfill

import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.jakewharton.rxbinding2.view.RxView
import com.tompee.utilities.filldevicespace.FillDeviceDiskApp
import com.tompee.utilities.filldevicespace.R
import com.tompee.utilities.filldevicespace.base.BaseFragment
import com.tompee.utilities.filldevicespace.di.component.DaggerMainComponent
import com.tompee.utilities.filldevicespace.feature.main.TouchInterceptor
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_easy_fill.*
import javax.inject.Inject

class EasyFillFragment : BaseFragment(), EasyFillView {
    @Inject
    lateinit var presenter: EasyFillPresenter

    //region Lifecycle
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        start.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.colorAccentLight))
        start.setImageResource(R.drawable.ic_play_arrow_white)
        presenter.attachView(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
    //endregion

    //region BaseFragment
    override fun setupComponent() {
        val component = DaggerMainComponent.builder()
                .appComponent((activity?.application as FillDeviceDiskApp).component)
                .build()
        component.inject(this)
    }

    override fun layoutId(): Int = R.layout.fragment_easy_fill
    //endregion

    //region View
    override fun startObservable(): Observable<Any> = RxView.clicks(start)

    override fun clearObservable(): Observable<Any> = RxView.clicks(clearFill)

    override fun setFreeSpace(space: String) {
        freeSpace.text = space
    }

    override fun setFillSpace(space: String) {
        fillSpace.text = space
    }

    override fun setPercentage(percentage: Double) {
        circleView.setValue(percentage.toFloat() * 100)
    }

    override fun setSpeed(text: String) {
        speed.text = text
    }

    override fun setFillState(state: Boolean) {
        clearFill.isEnabled = !state
        sdCard.isEnabled = !state
        if (state) {
            start.setImageResource(R.drawable.ic_stop_white)
        } else {
            start.setImageResource(R.drawable.ic_play_arrow_white)
        }
        (activity as TouchInterceptor).interceptTouchEvents(state)
    }
    //endregion
}