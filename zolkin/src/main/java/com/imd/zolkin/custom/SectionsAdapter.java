package com.imd.zolkin.custom;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class SectionsAdapter extends BaseAdapter
{
	public class IndexPath
	{
		public int row, section;
		
		public IndexPath(int row, int section)
		{
			this.row = row;
			this.section = section;
		}
	}
	
	public SectionsAdapter()
	{
		numSections = getNumSections();
		rowsPerSection = new int[numSections];
		for (int i = 0; i < rowsPerSection.length; i++)
		{
			rowsPerSection[i] = getRowsInSection(i);
		}
	}
	
	public int numSections;
	public int[] rowsPerSection;
	
	public abstract int getNumSections();
	public abstract int getRowsInSection(int section);
	public abstract Object getItem(int section, int row);
	public abstract View getView(int section, int row, View convertView, ViewGroup viewGroup);
	public abstract View getHeaderView(int section, View convertView, ViewGroup viewGroup);
	public abstract int getViewTypesCount();
	public abstract int getItemViewType(int section, int row);
	
	
	
	public IndexPath getSectionRowFromSimpleRow(int row)
	{
		
		for (int s = 0; s < numSections; s++)
		{
			row--; //discard section header
			if (row < 0)
			{
				return new IndexPath(-1, s);
			}
			if (row < rowsPerSection[s] && row >= 0)
			{
				return new IndexPath(row, s);
			}
			row -= rowsPerSection[s];
		}
		return null;
	}
	
	@Override
	public void notifyDataSetChanged()
	{
		numSections = getNumSections();
		rowsPerSection = new int[numSections];
		for (int i = 0; i < rowsPerSection.length; i++)
		{
			rowsPerSection[i] = getRowsInSection(i);
		}
		super.notifyDataSetChanged();
	}
	
	@Override
	public int getViewTypeCount()
	{
		return getViewTypesCount() + 1;
	}
	
	@Override
	public int getItemViewType(int position)
	{
		IndexPath ip = getSectionRowFromSimpleRow(position);
		if (ip.row > -1)
			return getItemViewType(ip.section, ip.row);
		else
			return -1;
	}

	@Override
	public int getCount()
	{
		int acc = 0;
		for (int i = 0; i < rowsPerSection.length; i++)
		{
			acc += rowsPerSection[i];
		}
		acc += numSections;
		return acc;
	}

	@Override
	public Object getItem(int position)
	{
		IndexPath ip = getSectionRowFromSimpleRow(position);
		
		return getItem(ip.section, ip.row);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup vg)
	{
		IndexPath ip = getSectionRowFromSimpleRow(position);
//		if (ip.row > -1)
//		{
//			return getHeaderView(ip.section, convertView, vg);
//		}
//		else
//		{
//			return getView(ip.section, ip.row, convertView, vg);
//		}
		
		if (ip.row == -1)
		{
			return getHeaderView(ip.section, convertView, vg);
		}
		else
		{
			return getView(ip.section, ip.row, convertView, vg);
		}
	}

}
