package com.tompee.utilities.filldevicespace.feature.main.advancefill

import com.tompee.utilities.filldevicespace.base.BasePresenter
import com.tompee.utilities.filldevicespace.core.helper.FormatHelper
import com.tompee.utilities.filldevicespace.interactor.FillInteractor
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

class AdvanceFillPresenter(private val fillInteractor: FillInteractor,
                           private val formatHelper: FormatHelper) : BasePresenter<AdvanceFillView>() {
    private var fillSubscription: Disposable? = null

    override fun onAttachView() {
        setupFreeSpaceTracker()
        setupFillSpaceTracker()
        setupPercentageTracker()
        setupSpeedTracker()
        setupFill()
        setupClear()
        setupDialLimit()
    }

    override fun onDetachView() {
    }

    private fun setupFreeSpaceTracker() {
        addSubscription(fillInteractor.getFreeSpaceObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.setFreeSpace(formatHelper.formatFileSize(it))
                }))
    }

    private fun setupFillSpaceTracker() {
        addSubscription(fillInteractor.getFillSpaceObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.setFillSpace(formatHelper.formatFileSize(it))
                }))
    }

    private fun setupPercentageTracker() {
        addSubscription(fillInteractor.getPercentageObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::setPercentage))
    }

    private fun setupSpeedTracker() {
        addSubscription(fillInteractor.getSpeedObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.setSpeed(formatHelper.formatSpeed(it))
                }))
    }

    private fun setupFill() {
        view.startObservable()
                .map({
                    fillSubscription = if (fillSubscription != null) {
                        fillSubscription?.dispose()
                        null
                    } else {
                        fillInteractor.startFill()
                                .subscribeOn(Schedulers.computation())
                                .doOnComplete({
                                    fillSubscription = null
                                    view.setFillState(false)
                                })
                                .subscribe()
                    }
                    return@map fillSubscription != null
                })
                .doOnNext(view::setFillState)
                .subscribe()
    }

    private fun setupClear() {
        addSubscription(view.clearObservable()
                .observeOn(Schedulers.computation())
                .map {
                    fillInteractor.clearFill()
                    return@map
                }
                .subscribe())
    }

    private fun setupDialLimit() {
        addSubscription(Observable.combineLatest(view.getGbObservable(), view.getMbObservable(),
                BiFunction<Int, Int, Long> { gb, mb ->
                    val mbValue = mb.toLong() * 1048576L
                    val gbValue = gb.toLong() * 1073741824L
                    return@BiFunction mbValue + gbValue
                })
                .map { it <= fillInteractor.getMaxStorageSpace() && it != 0L}
                .doOnNext(view::setStartButtonState)
                .subscribe())
    }
}