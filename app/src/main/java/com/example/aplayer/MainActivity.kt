package com.example.aplayer

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.aplayer.databinding.ActivityMainBinding
import com.example.aplayer.domain.service.PlayerService
import com.example.aplayer.presenter.tabs.TabsFragment
import java.util.regex.Pattern
const val Broadcast_PLAY_NEW_AUDIO = "com.example.aplayer.PlayNewAudio"
class MainActivity : AppCompatActivity() {

    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!
    private var navController: NavController? = null
    private val topLevelDestinations = setOf(getTabsDestination(), getSignInDestination())
    private var player: PlayerService? = null
    private var isServiceBound = false

    private val fragmentListener = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(
            fm: FragmentManager,
            f: Fragment,
            v: View,
            savedInstanceState: Bundle?
        ) {
            super.onFragmentViewCreated(fm, f, v, savedInstanceState)
            if (f is TabsFragment || f is NavHostFragment) return
            onNavControllerActivated(f.findNavController())
        }
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder: PlayerService.LocalBinder = service as PlayerService.LocalBinder
            player = binder.getService()
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Repositories.init(applicationContext)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.mainToolbar)
        val navController = getRootNavController()
        prepareRootNavController(true, navController)
        onNavControllerActivated(navController)

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentListener, true)
    }

    override fun onBackPressed() {
        if (isStartDestination(navController?.currentDestination)) {
            super.onBackPressed()
        } else {
            navController?.popBackStack()
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        (navController?.navigateUp() ?: false) || super.onSupportNavigateUp()

    private fun prepareRootNavController(isSignedIn: Boolean, navController: NavController) {
        val graph = navController.navInflater.inflate(getMainNavigationGraphId())
        graph.setStartDestination(
            if (isSignedIn) {
                getTabsDestination()
            } else {
                getSignInDestination()
            }
        )
        navController.graph = graph
    }

    private fun onNavControllerActivated(navController: NavController) {
        if (this.navController == navController) return
        this.navController?.removeOnDestinationChangedListener(destinationListener)
        navController.addOnDestinationChangedListener(destinationListener)
        this.navController = navController
    }

    private fun getRootNavController(): NavController {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        return navHost.navController
    }

    private val destinationListener =
        NavController.OnDestinationChangedListener { _, destination, arguments ->
            supportActionBar?.title = prepareTitle(destination.label, arguments)
            supportActionBar?.setDisplayHomeAsUpEnabled(!isStartDestination(destination))
        }

    private fun isStartDestination(destination: NavDestination?): Boolean {
        if (destination == null) return false
        val graph = destination.parent ?: return false
        val startDestinations = topLevelDestinations + graph.startDestinationId
        return startDestinations.contains(destination.id)
    }

    private fun prepareTitle(label: CharSequence?, arguments: Bundle?): String {
        if (label == null) return ""
        val title = StringBuffer()
        val fillInPattern = Pattern.compile("\\{(.+?)\\}")
        val matcher = fillInPattern.matcher(label)
        while (matcher.find()) {
            val argName = matcher.group(1)
            if (arguments != null && arguments.containsKey(argName)) {
                matcher.appendReplacement(title, "")
                title.append(arguments[argName].toString())
            } else {
                throw IllegalArgumentException(
                    "Could not find $argName in $arguments to fill label $label"
                )
            }
        }
        matcher.appendTail(title)
        return title.toString()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return false
    }

    private fun isSignedIn(): Boolean {
//        val bundle = intent.extras ?: throw IllegalStateException("No required arguments")
//        val args = MainActivityArgs.fromBundle(bundle)
//        return args.isSignedIn
        return true
    }

    private fun getMainNavigationGraphId(): Int = R.navigation.main_graph

    private fun getTabsDestination(): Int = R.id.tabsFragment

    private fun getSignInDestination(): Int = R.id.signInFragment

    override fun onDestroy() {
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentListener)
        navController = null
        super.onDestroy()
        mBinding = null
        if (isServiceBound) {
            unbindService(serviceConnection)
            //service is active
            player?.stopSelf()
        }
    }
}