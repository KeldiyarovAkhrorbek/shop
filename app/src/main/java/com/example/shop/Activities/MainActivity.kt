package com.example.shop.Activities

import android.content.Intent
import android.net.http.HttpException
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shop.R
import com.example.shop.RetrofitInstance
import com.example.shop.Shop
import com.example.shop.ShopAdapter
import com.example.shop.databinding.ActivityMainBinding
import java.io.IOException
import java.io.Serializable

const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var shopAdapter: ShopAdapter

    private var enabledShops: MutableList<Shop> = mutableListOf()


    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.root)

        setupRecyclerView()

        loadData()

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
        }

    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun loadData() {
        lifecycleScope.launchWhenCreated {
            binding.progressBar.isVisible = true
            binding.rvTodos.isVisible = false

            val response = try {
                RetrofitInstance.api.getShops()
            } catch (e: IOException) {
                Log.e(TAG, "IOException")
                return@launchWhenCreated
            } catch (e: HttpException) {
                Log.e(TAG, "HTTPException")
                return@launchWhenCreated
            }

            if (response.isSuccessful && response.body() != null) {
                shopAdapter.shops = response.body()!!.shops
            } else {
                Log.d(TAG, "Response not successfull")
            }
            binding.rvTodos.isVisible = true
            binding.progressBar.isVisible = false
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setupRecyclerView() = binding.rvTodos.apply {
        shopAdapter = ShopAdapter(object : ShopAdapter.OnShopCheckedChangeListener {
            override fun onShopChecked(shop: Shop, isChecked: Boolean) {
                if (isChecked) {
                    if (!enabledShops.contains(shop))
                        enabledShops.add(shop)
                } else {
                    if (enabledShops.contains(shop))
                        enabledShops.remove(shop)
                }
                Log.d(TAG, "onShopChecked: ${enabledShops.count()}")
            }
        })
        adapter = shopAdapter
        layoutManager = LinearLayoutManager(this@MainActivity)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.shops_view -> {
                if (enabledShops.isEmpty()) {
                    Toast.makeText(this, "Select at least one shop", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(this@MainActivity, ShowShopsActivity::class.java)
                    intent.putExtra("list", enabledShops as Serializable)
                    startActivity(intent)
                }

            }

            R.id.new_shop -> {
                Toast.makeText(this, "New view", Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }
}