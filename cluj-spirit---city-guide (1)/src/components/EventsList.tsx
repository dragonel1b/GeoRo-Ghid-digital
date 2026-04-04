import React from 'react';
import { SearchBar } from './SearchBar';
import { EventCard } from './EventCard';
import { EVENTS } from '../constants';

export const EventsList: React.FC = () => {
  return (
    <div className="pb-32 pt-8 space-y-8 max-w-md mx-auto">
      <SearchBar />
      
      <div className="px-6 grid grid-cols-2 gap-6">
        {EVENTS.map((event) => (
          <EventCard key={event.id} event={event} />
        ))}
      </div>
    </div>
  );
};
