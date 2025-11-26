// MainActivity.kt
package com.example.ioslauncher

import android.Manifest
import android.app.WallpaperManager
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    
    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var appAdapter: AppAdapter
    private var appList = mutableListOf<AppInfo>()
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupUI()
        checkPermissions()
        loadApps()
    }
    
    private fun setupUI() {
        appsRecyclerView = findViewById(R.id.appsRecyclerView)
        appsRecyclerView.layoutManager = GridLayoutManager(this, 4)
        
        // iOS-style status bar
        window.statusBarColor = ContextCompat.getColor(this, R.color.ios_gray)
        
        // Dock
        setupDock()
    }
    
    private fun setupDock() {
        val dockApps = findViewById<LinearLayout>(R.id.dockApps)
        // Add favorite apps to dock
    }
    
    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.SET_WALLPAPER
        )
        
        val ungrantedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (ungrantedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                ungrantedPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    
    private fun loadApps() {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        
        val packages = packageManager.queryIntentActivities(mainIntent, 0)
        packages.sortWith(Comparator { a, b ->
            String.CASE_INSENSITIVE_ORDER.compare(
                a.loadLabel(packageManager).toString(),
                b.loadLabel(packageManager).toString()
            )
        })
        
        appList.clear()
        packages.forEach {
            appList.add(AppInfo(
                it.loadLabel(packageManager).toString(),
                it.activityInfo.packageName,
                it.activityInfo.name,
                it.loadIcon(packageManager)
            ))
        }
        
        appAdapter = AppAdapter(appList)
        appsRecyclerView.adapter = appAdapter
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            loadApps()
        }
    }
    
    inner class AppAdapter(private val apps: List<AppInfo>) : 
        RecyclerView.Adapter<AppAdapter.AppViewHolder>() {
        
        inner class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.appIcon)
            val name: TextView = view.findViewById(R.id.appName)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app, parent, false)
            return AppViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
            val app = apps[position]
            holder.icon.setImageDrawable(app.icon)
            holder.name.text = app.name
            
            holder.itemView.setOnClickListener {
                launchApp(app)
            }
        }
        
        override fun getItemCount() = apps.size
    }
    
    private fun launchApp(app: AppInfo) {
        val intent = packageManager.getLaunchIntentForPackage(app.packageName)
        intent?.let {
            startActivity(it)
        }
    }
}

data class AppInfo(
    val name: String,
    val packageName: String,
    val className: String,
    val icon: Drawable
)