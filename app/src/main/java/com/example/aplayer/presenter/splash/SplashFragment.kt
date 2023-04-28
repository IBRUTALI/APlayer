package com.example.aplayer.presenter.splash

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.aplayer.R
import com.example.aplayer.databinding.FragmentSplashBinding
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class SplashFragment : Fragment(R.layout.fragment_splash) {
    private var mBinding: FragmentSplashBinding? = null
    private val binding get() = mBinding!!
    private val viewModel by lazy { SplashViewModel() }
    private var animator: ObjectAnimator? = null
    private val compositeDisposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressAnimation()
        startCounterProgress()
        animationListener()
        textObservable()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSplashBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private fun startCounterProgress() {
        val dispose = startProgress()
            .subscribeOn(AndroidSchedulers.mainThread())
            .delay(1500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                findNavController().navigate(R.id.tabsFragment)
            }, { throwable ->
                Log.e("!@#", throwable.message.toString())
            }
            )
        compositeDisposable.add(dispose)
    }

    private fun textObservable() {
        val dispose = startTextAnimation()
            .subscribeOn(Schedulers.newThread())
            .delay(50, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ count ->
                val text = "$count%"
                binding.splashProgressText.text = text
            },
                {})
        compositeDisposable.add(dispose)

    }

    private fun startProgress(): Completable {
        return Completable.create { subscriber ->
            animator!!.start()
            subscriber.onComplete()
        }
    }

    private fun startTextAnimation(): Observable<Int> {
        return Observable.create { subscriber ->
            for (i in 0..100) {
                Thread.sleep(1500/101)
                subscriber.onNext(i)
            }
        }
    }

    private fun progressAnimation() {
        binding.splashProgressBar.max = 30
        val value = viewModel.valueAnimationProgress
        animator = ObjectAnimator
            .ofInt(binding.splashProgressBar, "progress", value)
            .setDuration(1500)
    }

    private fun animationListener() {
        animator!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {}

            override fun onAnimationEnd(p0: Animator) {}

            override fun onAnimationCancel(p0: Animator) {}

            override fun onAnimationRepeat(p0: Animator) {
                startTextAnimation()
            }

        })
    }

    override fun onPause() {
        super.onPause()
        compositeDisposable.dispose()
        animator?.pause()
        viewModel.valueAnimationProgress = animator?.animatedValue as Int
    }

    override fun onResume() {
        super.onResume()
        animator?.resume()
        startCounterProgress()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }

}