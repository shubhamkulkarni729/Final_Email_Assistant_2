package com.cognition.android.mailboxapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CardArrayAdapter  extends ArrayAdapter<Card> {
    private List<Card> cardList = new ArrayList<>();
    Card card;

    static class CardViewHolder {
        TextView line1;
        TextView line2;
        TextView line3;
        TextView line4;
    }

    public CardArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    @Override
    public void add(Card object) {
        cardList.add(object);
        super.add(object);
    }

    @Override
    public void clear() {
        cardList.clear();
        super.clear();
    }

    @Override
    public int getCount() {
        return this.cardList.size();
    }

    @Override
    public Card getItem(int index) {
        return this.cardList.get(index);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        CardViewHolder viewHolder;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.list_item_card, parent, false);

            viewHolder = new CardViewHolder();
            viewHolder.line1 =  row.findViewById(R.id.line1);
            viewHolder.line2 =  row.findViewById(R.id.line2);
            viewHolder.line3 =  row.findViewById(R.id.line3);
            viewHolder.line4 =  row.findViewById(R.id.line4);

            row.setTag(viewHolder);
        }
        else {
            viewHolder = (CardViewHolder)row.getTag();
        }

        card = getItem(position);
        viewHolder.line1.setText(card.getSubject());
        viewHolder.line2.setText(card.getTime());
        viewHolder.line3.setText(card.getDate());
        viewHolder.line4.setText(card.getVenue());

        return row;
    }

}