export default interface Achievement {
  id: number;
  name: string;
  description: string;
  badgeImage: string;
  threshold: number;
  metric: Metric;
  actualDescription?: string;
}

export enum Metric {
  GAMES_PLAYED = 'GAMES_PLAYED', VICTORIES = 'VICTORIES', TOTAL_PLAY_TIME = 'TOTAL_PLAY_TIME', EXPLORER = 'EXPLORER',
  TOTAL_POINTS = 'TOTAL_POINTS'
}


export const emptyAchievement: Achievement = {id: 0, name: '', description: '', badgeImage: '',
    threshold: 0, metric: Metric.EXPLORER, actualDescription: '',};