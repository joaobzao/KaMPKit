//
//  BreedListView.swift
//  KaMPKitiOS
//
//  Created by Russell Wolf on 7/26/21.
//  Copyright © 2021 Touchlab. All rights reserved.
//

import Combine
import SwiftUI
import shared

private let log = koin.loggerWithTag(tag: "ViewController")

class ObservableBreedModel: ObservableObject {
    private var viewModel: BreedCallbackViewModel?

    @Published
    var loading = false

    @Published
    var breeds: [Breed]?

    @Published
    var error: String?

    private var cancellables = [AnyCancellable]()

    func activate() {
        let viewModel = KotlinDependencies.shared.getBreedViewModel()

        doPublish(viewModel.breeds) { [weak self] dogsState in
            // In swift 5.7 we could make it like:
            // ```
            // guard let self else {
            //     return assertionFailure("👻 ObservableBreedModel instance no longer exists")
            // }
            // ```
            guard let `self` = self else {
                return assertionFailure("👻 ObservableBreedModel instance no longer exists")
            }
            self.loading = dogsState.isLoading
            self.breeds = dogsState.breeds
            self.error = dogsState.error

            if let breeds = dogsState.breeds {
                log.d(message: {"View updating with \(breeds.count) breeds"})
            }
            if let errorMessage = dogsState.error {
                log.e(message: {"Displaying error: \(errorMessage)"})
            }
        }.store(in: &cancellables)

        self.viewModel = viewModel
    }

    func deactivate() {
        cancellables.forEach { $0.cancel() }
        cancellables.removeAll()

        viewModel?.clear()
        viewModel = nil
    }

    func onBreedFavorite(_ breed: Breed) {
        viewModel?.updateBreedFavorite(breed: breed)
    }
    
    func onBreedDelete(_ breed: Breed) {
        viewModel?.deleteBreed(breed: breed)
    }

    func refresh() {
        viewModel?.refreshBreeds()
    }
}

struct BreedListScreen: View {
    @StateObject
    var observableModel = ObservableBreedModel()

    var body: some View {
        BreedListContent(
            loading: observableModel.loading,
            breeds: observableModel.breeds,
            error: observableModel.error,
            onBreedFavorite: { observableModel.onBreedFavorite($0) },
            onBreedDelete: { observableModel.onBreedDelete($0) },
            refresh: { observableModel.refresh() }
        )
        .onAppear(perform: {
            observableModel.activate()
        })
        .onDisappear(perform: {
            observableModel.deactivate()
        })
    }
}

struct BreedListContent: View {
    var loading: Bool
    var breeds: [Breed]?
    var error: String?
    var onBreedFavorite: (Breed) -> Void
    var onBreedDelete: (Breed) -> Void
    var refresh: () -> Void

    var body: some View {
        ZStack {
            VStack {
                if let breeds = breeds {
                    List {
                        ForEach(breeds, id: \.id) { breed in
                            BreedRowView(breed: breed) {
                                onBreedFavorite(breed)
                            }
                        }
                        .onDelete(perform: delete(at:))
                    }
                }
                if let error = error {
                    Text(error)
                        .foregroundColor(.red)
                }
                Button("Refresh") {
                    refresh()
                }
            }
            if loading { Text("Loading...") }
        }
    }
    
    func delete(at offsets: IndexSet) {
        let index: Int = offsets[offsets.startIndex]
        guard let breed = breeds?[index]
        else {
            return assertionFailure("👻 trying to delete an item that does not exist.")
        }

        onBreedDelete(breed)
    }
}

struct BreedRowView: View {
    var breed: Breed
    var onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack {
                Text(breed.name)
                    .padding(4.0)
                Spacer()
                Image(systemName: !breed.favorite ? "heart" : "heart.fill")
                    .padding(4.0)
            }
        }
    }
}

struct BreedListScreen_Previews: PreviewProvider {
    static var previews: some View {
        BreedListContent(
            loading: false,
            breeds: [
                Breed(id: 0, name: "appenzeller", favorite: false),
                Breed(id: 1, name: "australian", favorite: true)
            ],
            error: nil,
            onBreedFavorite: { _ in },
            onBreedDelete: { _ in },
            refresh: {}
        )
    }
}
