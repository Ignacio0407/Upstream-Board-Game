export default interface Achievement {
  id: number;
  name: string;
  description: string;
  badgeImage: string;
  threshold: number;
  metric: string;
  actualDescription?: string;
}

export const emptyAchievement: Achievement = {id: 0, name: '', description: '', badgeImage: '',
    threshold: 1, metric: 'GAMES_PLAYED', actualDescription: '',};