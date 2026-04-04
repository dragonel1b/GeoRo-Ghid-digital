import React from 'react';
import { motion } from 'motion/react';
import { Event } from '../types';

interface EventCardProps {
  event: Event;
}

export const EventCard: React.FC<EventCardProps> = ({ event }) => {
  return (
    <motion.div
      whileHover={{ scale: 1.02 }}
      whileTap={{ scale: 0.98 }}
      className="relative group cursor-pointer"
    >
      {/* Braided Border Effect */}
      <div className="absolute inset-0 rounded-[2.5rem] p-[3px] bg-gradient-to-br from-cluj-gold via-cluj-accent to-cluj-gold opacity-80">
        <div className="absolute inset-0 rounded-[2.5rem] border-4 border-dashed border-cluj-dark/20 mix-blend-overlay" />
      </div>

      <div className="relative bg-cluj-dark rounded-[2.3rem] overflow-hidden m-[3px] h-full flex flex-col">
        <div className="relative h-64 overflow-hidden">
          <img
            src={event.imageUrl}
            alt={event.title}
            className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
            referrerPolicy="no-referrer"
          />
          <div className="absolute top-4 left-4">
            <span className="px-4 py-1.5 bg-black/40 backdrop-blur-md border border-white/20 rounded-full text-xs font-medium text-white">
              {event.date}
            </span>
          </div>
        </div>

        <div className="p-6 flex-grow flex flex-col justify-end bg-gradient-to-t from-cluj-dark via-cluj-dark/80 to-transparent">
          <h3 className="text-2xl font-display font-bold text-white mb-1 leading-tight">
            {event.title}
          </h3>
          <p className="text-sm text-gray-400 font-medium">
            {event.location}
          </p>
        </div>
      </div>
    </motion.div>
  );
};
