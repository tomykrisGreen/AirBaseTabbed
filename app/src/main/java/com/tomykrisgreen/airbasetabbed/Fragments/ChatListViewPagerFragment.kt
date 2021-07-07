package com.tomykrisgreen.airbasetabbed.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.tomykrisgreen.airbasetabbed.R


/**
 * A simple [Fragment] subclass.
 * Use the [ChatListViewPagerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatListViewPagerFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat_list_view_pager, container, false)

        val mViewPager = view.findViewById<View>(R.id.container_main) as ViewPager
        val mSectionsPagerAdapter = SectionsPagerAdapter(childFragmentManager)
        mViewPager.adapter = mSectionsPagerAdapter

        return view
    }

    private inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> return ChatListFragment()
                1 -> return GroupChatsFragment()
                else -> return UsersFragment()
            }
        }

        override fun getCount(): Int {
            return 3
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "Chats"
                1 -> return "Group Chats"
                else -> return "Users"
            }
        }
    }


}