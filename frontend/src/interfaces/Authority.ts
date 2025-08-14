export default interface Authority {
    id: number,
    userId: number,
    authority: string
}

export const emptyAuthority:Authority = {id: 0, userId: 0, authority: ""};