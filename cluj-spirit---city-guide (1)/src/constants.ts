import { Event, Category } from './types';

export const EVENTS: Event[] = [
  {
    id: '1',
    title: 'Untold Festival',
    date: '3-6 August 2024',
    location: 'Cluj-Napoca, RO',
    imageUrl: 'https://picsum.photos/seed/untold/400/600',
  },
  {
    id: '2',
    title: 'Teatrul Național',
    date: 'În seara asta, 19:00',
    location: 'Piesa "O noapte furtunoasă"',
    imageUrl: 'https://picsum.photos/seed/theater/400/600',
  },
  {
    id: '3',
    title: 'Jazz in the Park',
    date: '14-16 Iulie 2024',
    location: 'Parcul Central',
    imageUrl: 'https://picsum.photos/seed/jazz/400/600',
  },
  {
    id: '4',
    title: 'Street Food Carnival',
    date: 'Weekendul viitor',
    location: 'Iulius Parc',
    imageUrl: 'https://picsum.photos/seed/food/400/600',
  },
];

export const CATEGORIES: Category[] = [
  {
    id: '1',
    title: 'Atracții Turistice',
    description: 'Piața Unirii cu statuia lui Matei Corvin, Catedrala Sf. Mihail, Palatul Banffy...',
    imageUrl: 'https://picsum.photos/seed/attractions/400/300',
    icon: 'Camera',
  },
  {
    id: '2',
    title: 'Cultură și Festivaluri',
    description: 'Cluj-Napoca este capitala culturală, găzduind numeroase festivaluri',
    imageUrl: 'https://picsum.photos/seed/culture/400/300',
    icon: 'Music',
  },
  {
    id: '3',
    title: 'Viața Universitară',
    description: 'Descoperă spiritul academic al orașului',
    imageUrl: 'https://picsum.photos/seed/uni/400/300',
    icon: 'GraduationCap',
  },
  {
    id: '4',
    title: 'Natură și Parcuri',
    description: 'Grădina Botanică, Parcul Central și malul Someșului',
    imageUrl: 'https://picsum.photos/seed/nature/400/300',
    icon: 'Leaf',
  },
];
